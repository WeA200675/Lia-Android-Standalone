package de.wea200675.lia

import de.wea200675.lia.admin.*
import org.junit.Assert.*
import org.junit.Test
import java.security.SecureRandom

class AdminPinSecurityTest {
    @Test fun saltedVerifierAcceptsOnlyCorrectBoundedNumericPin() {
        val seeded = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(42L) }
        val verifier = AdminPinVerifier.create("123456", seeded)

        assertTrue(AdminPinVerifier.verify("123456", verifier))
        assertFalse(AdminPinVerifier.verify("123457", verifier))
        assertFalse(AdminPinVerifier.verify("12345", verifier))
        assertFalse(verifier.contains("123456"))
    }

    @Test fun equalPinsReceiveDifferentRandomSalts() {
        val first = AdminPinVerifier.create("654321")
        val second = AdminPinVerifier.create("654321")
        assertNotEquals(first, second)
        assertTrue(AdminPinVerifier.verify("654321", first))
        assertTrue(AdminPinVerifier.verify("654321", second))
    }

    @Test fun fiveFailuresCreateTemporaryRecoverableLockout() {
        val limiter = AdminPinAttemptLimiter(maxFailures = 5, lockoutMs = 60_000L)
        var state = AdminPinAttemptState()
        repeat(5) { state = limiter.recordFailure(state, 1_000L) }

        assertTrue(limiter.isLocked(state, 1_000L))
        assertEquals(60L, limiter.remainingSeconds(state, 1_000L))
        assertFalse(limiter.isLocked(state, 61_001L))
        state = limiter.recordFailure(state, 61_001L)
        assertEquals(1, state.failures)
        assertFalse(limiter.isLocked(state, 61_001L))
    }

    @Test fun successfulPinClearsFailureBudget() {
        val limiter = AdminPinAttemptLimiter()
        val failed = limiter.recordFailure(AdminPinAttemptState(), 1_000L)
        assertEquals(AdminPinAttemptState(), limiter.recordSuccess())
        assertEquals(1, failed.failures)
    }

    @Test fun malformedVerifierFailsClosed() {
        val malformed = listOf("v1", "999999999", "bad", "bad").joinToString("$")
        assertFalse(AdminPinVerifier.verify("123456", malformed))
        assertFalse(AdminPinVerifier.verify("123456", "not-a-verifier"))
    }
}
