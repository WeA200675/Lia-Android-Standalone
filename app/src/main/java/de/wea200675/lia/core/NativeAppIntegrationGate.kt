package de.wea200675.lia.core

data class NativeAppEvidence(
    val modelSha256Verified: Boolean,
    val nativeLibraryVerified: Boolean,
    val nativeSessionHealthy: Boolean,
    val offlineFallbackAvailable: Boolean,
    val feedbackPersistenceAvailable: Boolean,
    val adminStatusAvailable: Boolean
)

data class NativeAppIntegrationResult(
    val nativeReady: Boolean,
    val appReady: Boolean,
    val overallReady: Boolean,
    val reason: String
)

object NativeAppIntegrationGate {
    fun evaluate(evidence: NativeAppEvidence): NativeAppIntegrationResult {
        val nativeReady = evidence.modelSha256Verified &&
            evidence.nativeLibraryVerified &&
            evidence.nativeSessionHealthy
        val appReady = evidence.offlineFallbackAvailable &&
            evidence.feedbackPersistenceAvailable &&
            evidence.adminStatusAvailable
        val reason = when {
            !evidence.offlineFallbackAvailable ->
                "Offline-Fallback fehlt; Integration bleibt BLOCKED."
            !nativeReady ->
                "Native Artefakte oder native Sitzung nicht verifiziert; Offline-Modus bleibt aktiv."
            !appReady ->
                "App-Integration unvollständig; sichere lokale Bedienung bleibt erforderlich."
            else -> "Native Laufzeit und App-Integration sind verifiziert."
        }
        return NativeAppIntegrationResult(nativeReady, appReady, nativeReady && appReady, reason)
    }
}
