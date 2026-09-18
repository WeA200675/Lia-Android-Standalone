package de.wea200675.lia.admin

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AdminPinAttemptState(val failures: Int = 0, val lockedUntilEpochMs: Long = 0L)

class AdminPinAttemptLimiter(
    private val maxFailures: Int = 5,
    private val lockoutMs: Long = 5 * 60 * 1000L
) {
    init { require(maxFailures in 3..10); require(lockoutMs in 30_000L..30 * 60 * 1000L) }

    fun isLocked(state: AdminPinAttemptState, nowEpochMs: Long): Boolean =
        state.lockedUntilEpochMs > nowEpochMs

    fun recordFailure(state: AdminPinAttemptState, nowEpochMs: Long): AdminPinAttemptState {
        val failures = if (state.lockedUntilEpochMs != 0L && nowEpochMs >= state.lockedUntilEpochMs) 1
            else state.failures + 1
        return if (failures >= maxFailures) AdminPinAttemptState(0, nowEpochMs + lockoutMs)
            else AdminPinAttemptState(failures, 0L)
    }

    fun recordSuccess(): AdminPinAttemptState = AdminPinAttemptState()

    fun remainingSeconds(state: AdminPinAttemptState, nowEpochMs: Long): Long =
        ((state.lockedUntilEpochMs - nowEpochMs).coerceAtLeast(0L) + 999L) / 1000L
}

object AdminPinVerifier {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val PREFIX = "v1"

    fun create(pin: String, random: SecureRandom = SecureRandom()): String {
        requireValidPin(pin)
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(pin, salt, ITERATIONS)
        return listOf(PREFIX, ITERATIONS.toString(), b64(salt), b64(hash)).joinToString("$")
    }

    fun verify(pin: String, verifier: String): Boolean {
        return try {
            if (!isValidPin(pin)) {
                false
            } else {
                val parts = verifier.split('$')
                val iterations = parts.getOrNull(1)?.toIntOrNull()
                if (parts.size != 4 || parts[0] != PREFIX || iterations == null || iterations !in 100_000..500_000) {
                    false
                } else {
                    val salt = Base64.getDecoder().decode(parts[2])
                    val expected = Base64.getDecoder().decode(parts[3])
                    salt.size == SALT_BYTES && expected.size == KEY_BITS / 8 &&
                        MessageDigest.isEqual(expected, derive(pin, salt, iterations))
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun requireValidPin(pin: String) {
        require(isValidPin(pin)) { "Admin PIN must contain 6 to 12 digits" }
    }

    private fun isValidPin(pin: String) = pin.matches(Regex("""\d{6,12}"""))

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun b64(value: ByteArray) = Base64.getEncoder().withoutPadding().encodeToString(value)
}
