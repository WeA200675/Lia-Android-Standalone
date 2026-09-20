package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeGatewayAdmission
import de.wea200675.lia.core.KnowledgeGatewayEvidence
import org.junit.Assert.*
import org.junit.Test

class KnowledgeGatewayAdmissionTest {
    @Test
    fun missingAnonymizationDeniesNetwork() {
        val result = KnowledgeGatewayAdmission.evaluate(
            KnowledgeGatewayEvidence(false, true, true, true, true, true)
        )
        assertFalse(result.networkAllowed)
        assertFalse(result.ready)
    }

    @Test
    fun missingOfflineFallbackBlocksGateway() {
        val result = KnowledgeGatewayAdmission.evaluate(
            KnowledgeGatewayEvidence(true, true, true, true, true, false)
        )
        assertFalse(result.offlineSafe)
        assertTrue(result.reason.contains("BLOCKED"))
    }

    @Test
    fun allEvidenceIsRequired() {
        val result = KnowledgeGatewayAdmission.evaluate(
            KnowledgeGatewayEvidence(true, true, true, true, true, true)
        )
        assertTrue(result.networkAllowed)
        assertTrue(result.offlineSafe)
        assertTrue(result.ready)
    }
}
