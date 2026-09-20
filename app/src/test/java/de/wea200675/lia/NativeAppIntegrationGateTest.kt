package de.wea200675.lia

import de.wea200675.lia.core.NativeAppEvidence
import de.wea200675.lia.core.NativeAppIntegrationGate
import org.junit.Assert.*
import org.junit.Test

class NativeAppIntegrationGateTest {
    @Test
    fun missingNativeArtifactsKeepOfflineAppSafe() {
        val result = NativeAppIntegrationGate.evaluate(
            NativeAppEvidence(false, false, false, true, true, true)
        )
        assertFalse(result.nativeReady)
        assertTrue(result.appReady)
        assertFalse(result.overallReady)
        assertTrue(result.reason.contains("Offline-Modus"))
    }

    @Test
    fun nativeReadinessRequiresAllThreeNativeChecks() {
        val base = NativeAppEvidence(true, true, true, true, true, true)
        assertTrue(NativeAppIntegrationGate.evaluate(base).overallReady)
        assertFalse(NativeAppIntegrationGate.evaluate(base.copy(nativeSessionHealthy = false)).nativeReady)
    }

    @Test
    fun missingOfflineFallbackBlocksEverything() {
        val result = NativeAppIntegrationGate.evaluate(NativeAppEvidence(true, true, true, false, true, true))
        assertFalse(result.overallReady)
        assertTrue(result.reason.contains("BLOCKED"))
    }
}
