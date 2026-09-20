package de.wea200675.lia

import de.wea200675.lia.core.RoadmapEvidence
import de.wea200675.lia.core.RoadmapPhase
import org.junit.Assert.*
import org.junit.Test

class RoadmapPhaseTest {
    @Test
    fun phasesAreOrderedSixThroughTen() {
        assertEquals((6..10).toList(), RoadmapPhase.entries.map { it.id })
    }

    @Test
    fun evidenceDoesNotClaimUnverifiedHardwareOrArtifacts() {
        val evidence = RoadmapEvidence()
        assertTrue(evidence.completed().isEmpty())
        RoadmapPhase.entries.forEach { phase -> assertFalse(evidence.isComplete(phase)) }
    }

    @Test
    fun eachEvidenceFlagMapsToExactlyOnePhase() {
        val evidence = RoadmapEvidence(
            nativeArtifactsVerified = true,
            appIntegrationVerified = true,
            anonymizedGatewayVerified = true,
            samsungAcceptanceVerified = true,
            releaseHardeningVerified = true
        )
        assertEquals(RoadmapPhase.entries.toSet(), evidence.completed())
    }
}
