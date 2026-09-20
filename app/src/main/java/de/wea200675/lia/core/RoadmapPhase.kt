package de.wea200675.lia.core

/**
 * The five remaining delivery phases after the initial integration gates.
 *
 * A phase is only considered complete when its evidence is supplied and verified;
 * this prevents the UI from claiming native inference or hardware acceptance that
 * is not actually present on the device.
 */
enum class RoadmapPhase(val id: Int, val title: String) {
    NATIVE_ARTIFACTS(6, "Native Android LLM artifacts"),
    APP_INTEGRATION(7, "End-to-end app integration"),
    ANONYMIZED_GATEWAY(8, "Anonymized knowledge gateway"),
    SAMSUNG_ACCEPTANCE(9, "Samsung hardware acceptance"),
    RELEASE_HARDENING(10, "Release and recovery hardening")
}

data class RoadmapEvidence(
    val nativeArtifactsVerified: Boolean = false,
    val appIntegrationVerified: Boolean = false,
    val anonymizedGatewayVerified: Boolean = false,
    val samsungAcceptanceVerified: Boolean = false,
    val releaseHardeningVerified: Boolean = false
) {
    fun completed(): Set<RoadmapPhase> = buildSet {
        if (nativeArtifactsVerified) add(RoadmapPhase.NATIVE_ARTIFACTS)
        if (appIntegrationVerified) add(RoadmapPhase.APP_INTEGRATION)
        if (anonymizedGatewayVerified) add(RoadmapPhase.ANONYMIZED_GATEWAY)
        if (samsungAcceptanceVerified) add(RoadmapPhase.SAMSUNG_ACCEPTANCE)
        if (releaseHardeningVerified) add(RoadmapPhase.RELEASE_HARDENING)
    }

    fun isComplete(phase: RoadmapPhase): Boolean = phase in completed()
}
