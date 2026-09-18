package de.wea200675.lia.admin

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.WindowManager
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class KioskController(
    private val activity: Activity,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val prefs: SharedPreferences = activity.getSharedPreferences("lia_admin", Context.MODE_PRIVATE)
    private val limiter = AdminPinAttemptLimiter()

    fun isEnabled() = prefs.getBoolean("kiosk_enabled", false)

    fun enable() {
        prefs.edit().putBoolean("kiosk_enabled", true).apply()
        activity.startLockTask()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun disableWithPin(pin: String): Boolean {
        val now = clock()
        val attemptState = loadAttemptState()
        if (limiter.isLocked(attemptState, now)) return false

        val verifier = prefs.getString(KEY_PIN_VERIFIER, null)
        val legacyHash = prefs.getString(KEY_LEGACY_HASH, null)
        val valid = when {
            verifier != null -> AdminPinVerifier.verify(pin, verifier)
            legacyHash != null -> verifyLegacy(pin, legacyHash)
            else -> false
        }
        if (!valid) {
            saveAttemptState(limiter.recordFailure(attemptState, now))
            return false
        }

        val editor = prefs.edit()
            .putInt(KEY_FAILURES, 0)
            .putLong(KEY_LOCKED_UNTIL, 0L)
            .putBoolean("kiosk_enabled", false)
        if (verifier == null) {
            // Migrate only after successful legacy verification; never weaken or reset an unknown PIN.
            editor.putString(KEY_PIN_VERIFIER, AdminPinVerifier.create(pin)).remove(KEY_LEGACY_HASH)
        }
        check(editor.commit()) { "Admin security state could not be persisted" }
        activity.stopLockTask()
        return true
    }

    fun lockoutRemainingSeconds(): Long =
        limiter.remainingSeconds(loadAttemptState(), clock())

    fun setAdminPin(pin: String) {
        AdminPinVerifier.requireValidPin(pin)
        check(
            prefs.edit()
                .putString(KEY_PIN_VERIFIER, AdminPinVerifier.create(pin))
                .remove(KEY_LEGACY_HASH)
                .putInt(KEY_FAILURES, 0)
                .putLong(KEY_LOCKED_UNTIL, 0L)
                .commit()
        ) { "Admin PIN could not be persisted" }
    }

    private fun loadAttemptState() = AdminPinAttemptState(
        failures = prefs.getInt(KEY_FAILURES, 0).coerceAtLeast(0),
        lockedUntilEpochMs = prefs.getLong(KEY_LOCKED_UNTIL, 0L).coerceAtLeast(0L)
    )

    private fun saveAttemptState(state: AdminPinAttemptState) {
        check(
            prefs.edit()
                .putInt(KEY_FAILURES, state.failures)
                .putLong(KEY_LOCKED_UNTIL, state.lockedUntilEpochMs)
                .commit()
        ) { "Admin attempt state could not be persisted" }
    }

    private fun verifyLegacy(pin: String, storedHash: String): Boolean {
        if (!pin.matches(Regex("""\d{6,12}""")) || !storedHash.matches(Regex("[a-f0-9]{64}"))) return false
        val actual = MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(
            actual.toByteArray(StandardCharsets.US_ASCII),
            storedHash.toByteArray(StandardCharsets.US_ASCII)
        )
    }

    private companion object {
        const val KEY_PIN_VERIFIER = "admin_pin_verifier_v1"
        const val KEY_LEGACY_HASH = "admin_pin_hash"
        const val KEY_FAILURES = "admin_pin_failures"
        const val KEY_LOCKED_UNTIL = "admin_pin_locked_until"
    }
}
