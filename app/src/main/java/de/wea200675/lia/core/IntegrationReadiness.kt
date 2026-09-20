package de.wea200675.lia.core

enum class IntegrationArea { NATIVE_LLM, FEEDBACK_UI, ADMIN_EVOLUTION, KNOWLEDGE_GATEWAY, SAMSUNG_ACCEPTANCE }

data class IntegrationReadiness(
    val area: IntegrationArea,
    val ready: Boolean,
    val reason: String
)

object IntegrationReadinessEvaluator {
    fun nativeLlm(modelFilePresent: Boolean, jniLibraryPresent: Boolean, hashesVerified: Boolean): IntegrationReadiness =
        if (modelFilePresent && jniLibraryPresent && hashesVerified)
            IntegrationReadiness(IntegrationArea.NATIVE_LLM, true, "Native LLM-Artefakte geprüft und bereit")
        else
            IntegrationReadiness(IntegrationArea.NATIVE_LLM, false, "Offline-Fallback bleibt aktiv: Native Bibliothek, Modell oder Hashprüfung fehlt")

    fun feedbackUi(connected: Boolean): IntegrationReadiness =
        IntegrationReadiness(IntegrationArea.FEEDBACK_UI, connected, if (connected) "Feedback-Eingabe verbunden" else "Feedback-Eingabe noch nicht verbunden")

    fun adminEvolution(visible: Boolean, resetProtected: Boolean): IntegrationReadiness =
        IntegrationReadiness(IntegrationArea.ADMIN_EVOLUTION, visible && resetProtected, if (visible && resetProtected) "Entwicklungsstand geschützt sichtbar" else "Admin-Anzeige oder PIN-Schutz fehlt")

    fun knowledgeGateway(sourceCount: Int, provenance: Boolean, fallback: Boolean): IntegrationReadiness =
        IntegrationReadiness(IntegrationArea.KNOWLEDGE_GATEWAY, sourceCount > 0 && provenance && fallback, "Quellen müssen Provenienz und Offline-Fallback liefern")

    fun samsungAcceptance(android16: Boolean, speech: Boolean, kiosk: Boolean, wifi: Boolean): IntegrationReadiness =
        IntegrationReadiness(IntegrationArea.SAMSUNG_ACCEPTANCE, android16 && speech && kiosk && wifi, "Android-16-, Sprache-, Kiosk- und WLAN-Prüfung erforderlich")
}
