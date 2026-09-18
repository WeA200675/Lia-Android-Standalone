package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeSourceHealthTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSourceHealthTrackerTest {
    @Test fun sourceRemainsAvailableBeforeFailureThreshold() {
        val tracker = KnowledgeSourceHealthTracker(failureThreshold = 3, clock = { 1_000L })
        tracker.recordFailure("source")
        tracker.recordFailure("source")

        assertTrue(tracker.canAttempt("source"))
        assertEquals(2, tracker.status("source").consecutiveFailures)
    }

    @Test fun repeatedFailuresOpenCircuitTemporarily() {
        var now = 1_000L
        val tracker = KnowledgeSourceHealthTracker(
            failureThreshold = 3,
            cooldownMs = 60_000L,
            clock = { now }
        )
        repeat(3) { tracker.recordFailure("source") }

        assertFalse(tracker.canAttempt("source"))
        now += 60_000L
        assertTrue(tracker.canAttempt("source"))
    }

    @Test fun successfulProbeFullyRestoresSource() {
        val tracker = KnowledgeSourceHealthTracker(failureThreshold = 2, clock = { 1_000L })
        tracker.recordFailure("source")
        tracker.recordSuccess("source")

        assertTrue(tracker.canAttempt("source"))
        assertEquals(0, tracker.status("source").consecutiveFailures)
    }

    @Test fun oneSourceFailureDoesNotBlockAnother() {
        val tracker = KnowledgeSourceHealthTracker(failureThreshold = 1, clock = { 1_000L })
        tracker.recordFailure("broken")

        assertFalse(tracker.canAttempt("broken"))
        assertTrue(tracker.canAttempt("healthy"))
    }

    @Test fun resetRestoresAllSources() {
        val tracker = KnowledgeSourceHealthTracker(failureThreshold = 1, clock = { 1_000L })
        tracker.recordFailure("one")
        tracker.recordFailure("two")
        tracker.reset()

        assertTrue(tracker.canAttempt("one"))
        assertTrue(tracker.canAttempt("two"))
    }
}
