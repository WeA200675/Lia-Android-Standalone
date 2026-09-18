package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeCacheCapacity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeCacheCapacityTest {
    @Test fun lowMemoryDeviceStaysSmall() {
        val plan = KnowledgeCacheCapacity.recommend(ramMb = 3_072, sourceCount = 17)
        assertEquals(24, plan.maxEntries)
        assertEquals("sparsam", plan.profile)
    }

    @Test fun eightGigabyteTabletUsesExtendedBoundedProfile() {
        val plan = KnowledgeCacheCapacity.recommend(ramMb = 8_192, sourceCount = 17)
        assertEquals(102, plan.maxEntries)
        assertEquals("erweitert", plan.profile)
        assertTrue(plan.estimatedMaxKilobytes <= 640)
    }

    @Test fun sourceGrowthCanExpandWithinHardwareCeiling() {
        val normal = KnowledgeCacheCapacity.recommend(ramMb = 16_384, sourceCount = 17)
        val expanded = KnowledgeCacheCapacity.recommend(ramMb = 16_384, sourceCount = 40)
        assertTrue(expanded.maxEntries > normal.maxEntries)
        assertTrue(expanded.maxEntries <= 256)
    }

    @Test fun storageSizeDoesNotCauseUnboundedRamUse() {
        val plan = KnowledgeCacheCapacity.recommend(ramMb = 65_536, sourceCount = 10_000)
        assertEquals(256, plan.maxEntries)
        assertEquals(1_024, plan.estimatedMaxKilobytes)
    }
}
