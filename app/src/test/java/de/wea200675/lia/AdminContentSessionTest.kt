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
}
