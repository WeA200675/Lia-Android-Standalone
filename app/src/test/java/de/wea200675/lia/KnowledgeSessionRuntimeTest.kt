package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeFreshnessPolicy
import de.wea200675.lia.core.KnowledgeSessionRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KnowledgeSessionRuntimeTest {
    @Before fun reset() {
        KnowledgeSessionRuntime.reset()
        KnowledgeSessionRuntime.configureCache(32)
    }

    @Test fun snapshotReportsCacheCapacityAndUsage() {
        KnowledgeSessionRuntime.cache().put(
            "Frage",
            "Antwort",
            KnowledgeFreshnessPolicy.HOUR_MS
        )
        val snapshot = KnowledgeSessionRuntime.snapshot()

        assertEquals(1, snapshot.cachedEntries)
        assertEquals(32, snapshot.cacheCapacity)
    }

    @Test fun snapshotReportsSourceFailuresAndSuspensions() {
        repeat(3) { KnowledgeSessionRuntime.sourceHealth.recordFailure("source") }
        val snapshot = KnowledgeSessionRuntime.snapshot()

        assertEquals(1, snapshot.sourcesWithFailures)
        assertEquals(1, snapshot.suspendedSources)
    }

    @Test fun resetClearsCacheAndSourceHealthWithoutChangingCapacity() {
        val cache = KnowledgeSessionRuntime.configureCache(96)
        cache.put("Frage", "Antwort", KnowledgeFreshnessPolicy.HOUR_MS)
        KnowledgeSessionRuntime.sourceHealth.recordFailure("source")

        KnowledgeSessionRuntime.reset()
        val snapshot = KnowledgeSessionRuntime.snapshot()

        assertEquals(0, snapshot.cachedEntries)
        assertEquals(96, snapshot.cacheCapacity)
        assertEquals(0, snapshot.sourcesWithFailures)
        assertTrue(KnowledgeSessionRuntime.sourceHealth.canAttempt("source"))
    }
}
