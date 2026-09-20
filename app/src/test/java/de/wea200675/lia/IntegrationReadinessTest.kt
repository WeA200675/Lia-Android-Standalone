package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test

class IntegrationReadinessTest {
    @Test fun nativeArtifactsMissingKeepOfflineSafe() {
        val result = IntegrationReadinessEvaluator.nativeLlm(false, false, false)
        assertFalse(result.ready)
        assertTrue(result.reason.contains("Offline-Fallback"))
    }

    @Test fun allIntegrationAreasRequireTheirSafetyGates() {
        assertFalse(IntegrationReadinessEvaluator.feedbackUi(false).ready)
        assertTrue(IntegrationReadinessEvaluator.adminEvolution(true, true).ready)
        assertFalse(IntegrationReadinessEvaluator.knowledgeGateway(0, true, true).ready)
        assertFalse(IntegrationReadinessEvaluator.samsungAcceptance(true, true, false, true).ready)
    }

    @Test fun samsungAcceptanceNeedsAllRealDeviceChecks() {
        assertTrue(IntegrationReadinessEvaluator.samsungAcceptance(true, true, true, true).ready)
    }
}
