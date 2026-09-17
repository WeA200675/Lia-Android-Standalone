package de.wea200675.lia

import de.wea200675.lia.core.RecoveryLevel
import de.wea200675.lia.core.SelfHealingPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SelfHealingPolicyTest {
    @Test fun backsOffAndStopsAfterBudget() {
        val policy = SelfHealingPolicy(maxRestarts = 3, physicalCores = 8, logicalThreads = 16)
        assertEquals(RecoveryLevel.FULL, policy.decide(0).level)
        assertEquals(16, policy.decide(0).threads)
        assertEquals(RecoveryLevel.REDUCED, policy.decide(1).level)
        assertEquals(RecoveryLevel.SAFE, policy.decide(2).level)
        assertEquals(8, policy.decide(2).threads)
        assertEquals(RecoveryLevel.BLOCKED, policy.decide(3).level)
        assertFalse(policy.decide(3).restartAllowed)
    }
}
