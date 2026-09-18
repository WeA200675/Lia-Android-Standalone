package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.RecoveryLevel
import de.wea200675.lia.core.RuntimeCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RuntimeCoordinatorTest {
    private fun coordinator() = RuntimeCoordinator(
        ModelSpec("primary", "primary.gguf", "x", 4096),
        ModelSpec("recovery", "recovery.gguf", "y", 2048),
        physicalCores = 4,
        logicalThreads = 8
    )

    @Test fun failureUpdatesRecoveryDecisionAndBudget() {
        val coordinator = coordinator()
        coordinator.recordFailure()
        assertEquals("BACKOFF", coordinator.state)
        assertEquals(RecoveryLevel.REDUCED, coordinator.lastRecovery.level)
        assertEquals(2_000L, coordinator.lastRecovery.delayMillis)
        assertEquals(2, coordinator.restartBudget)
    }

    @Test fun restartBudgetEndsInBlockedState() {
        val coordinator = coordinator()
        repeat(3) { coordinator.recordFailure() }
        assertEquals("BLOCKED", coordinator.state)
        assertEquals(RecoveryLevel.BLOCKED, coordinator.lastRecovery.level)
        assertFalse(coordinator.lastRecovery.restartAllowed)
        assertEquals(0, coordinator.restartBudget)
    }
}
