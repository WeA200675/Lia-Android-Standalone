package de.wea200675.lia

import de.wea200675.lia.core.ModelSpec
import de.wea200675.lia.core.RuntimeCoordinator
import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeCoordinatorTest {
    @Test fun failureUpdatesRecoveryDecisionAndBudget() {
        val coordinator = RuntimeCoordinator(
            ModelSpec("primary", "primary.gguf", "x", 4096),
            ModelSpec("recovery", "recovery.gguf", "y", 2048),
            physicalCores = 4,
            logicalThreads = 8
        )
        coordinator.recordFailure()
        assertEquals("BACKOFF", coordinator.state)
        assertEquals(2_000L, coordinator.lastRecovery.delayMillis)
        assertEquals(2, coordinator.restartBudget)
    }
}
