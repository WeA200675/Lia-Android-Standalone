package de.wea200675.lia

import de.wea200675.lia.admin.AdminContentSession
import org.junit.Assert.*
import org.junit.Test

class AdminContentSessionTest {
    @Test fun lockedSessionNeverReadsPersonalStorage() {
        val session = AdminContentSession()
        assertNull(session.read<String> { error("Personal storage must not be opened") })
        assertFalse(session.isUnlocked)
    }

    @Test fun leavingAdminRevokesAccessUntilFreshVerification() {
        val session = AdminContentSession()
        var reads = 0
        session.unlock()
        assertEquals("private answer", session.read { reads++; "private answer" })
        session.lock()
        assertNull(session.read { reads++; "private answer" })
        assertEquals(1, reads)
        session.unlock()
        assertEquals("private answer", session.read { reads++; "private answer" })
        assertEquals(2, reads)
    }

    @Test fun recreatedActivityDoesNotInheritAuthorization() {
        val previous = AdminContentSession()
        previous.unlock()
        assertFalse(AdminContentSession().isUnlocked)
    }

    @Test fun expiresExactlyAtDeadlineWithoutReadingStorage() {
        var now = 100L
        val session = AdminContentSession(nowMillis = { now }, lifetimeMillis = 1000)
        session.unlock()
        now = 1099
        assertEquals("allowed", session.read { "allowed" })
        now = 1100
        assertNull(session.read<String> { error("Expired storage read") })
        assertFalse(session.isUnlocked)
    }

    @Test fun readingDoesNotExtendTheSession() {
        var now = 0L
        val session = AdminContentSession(nowMillis = { now }, lifetimeMillis = 1000)
        session.unlock()
        now = 900
        assertTrue(session.isUnlocked)
        assertEquals("data", session.read { "data" })
        now = 1000
        assertFalse(session.isUnlocked)
        session.unlock()
        assertTrue(session.isUnlocked)
        now = 2000
        assertFalse(session.isUnlocked)
    }

    @Test fun backwardsClockRevokesUntilFreshAuthentication() {
        var now = 100L
        val session = AdminContentSession(nowMillis = { now })
        session.unlock()
        now = 99
        assertFalse(session.isUnlocked)
        now = 101
        assertFalse(session.isUnlocked)
    }

    @Test fun explicitLockCancelsAccessBeforeDeadline() {
        val session = AdminContentSession(nowMillis = { 0L })
        session.unlock()
        session.lock()
        assertNull(session.read<String> { error("Locked storage read") })
    }
}
