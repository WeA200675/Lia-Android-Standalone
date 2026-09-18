package de.wea200675.lia

import de.wea200675.lia.core.AdminDestructiveAction
import de.wea200675.lia.core.AdminDestructiveActionGuard
import org.junit.Assert.*
import org.junit.Test

class AdminDestructiveActionGuardTest {
    @Test fun sameActionRequiresSecondRequestInsideWindow() {
        var now = 1_000L
        val guard = AdminDestructiveActionGuard(clock = { now })
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
        now += 10_000L
        assertTrue(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
    }

    @Test fun differentActionCannotConfirmArmedDeletion() {
        val guard = AdminDestructiveActionGuard(clock = { 1_000L })
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE))
        assertTrue(guard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE))
    }

    @Test fun expiredOrBackwardClockRequiresFreshConfirmation() {
        var now = 10_000L
        val guard = AdminDestructiveActionGuard(confirmationWindowMs = 5_000L, clock = { now })
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE))
        now = 16_000L
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE))
        now = 15_000L
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_CONFIRMED_KNOWLEDGE))
    }

    @Test fun cancelDisarmsPendingDeletion() {
        val guard = AdminDestructiveActionGuard(clock = { 1_000L })
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
        guard.cancel()
        assertFalse(guard.confirm(AdminDestructiveAction.DELETE_LEARNING_PROFILE))
    }
}
