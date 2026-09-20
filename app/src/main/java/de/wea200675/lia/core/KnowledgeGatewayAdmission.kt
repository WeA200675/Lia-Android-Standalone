package de.wea200675.lia.core

data class KnowledgeGatewayEvidence(
    val queryAnonymized: Boolean,
    val policyAllowsNetwork: Boolean,
    val consentRecorded: Boolean,
    val provenanceAvailable: Boolean,
    val boundedCacheAvailable: Boolean,
    val offlineFallbackAvailable: Boolean
)

data class KnowledgeGatewayAdmissionResult(
    val networkAllowed: Boolean,
    val offlineSafe: Boolean,
    val ready: Boolean,
    val reason: String
)

object KnowledgeGatewayAdmission {
    fun evaluate(evidence: KnowledgeGatewayEvidence): KnowledgeGatewayAdmissionResult {
        val networkAllowed = evidence.queryAnonymized &&
            evidence.policyAllowsNetwork &&
            evidence.consentRecorded
        val offlineSafe = evidence.boundedCacheAvailable && evidence.offlineFallbackAvailable
        val ready = networkAllowed && evidence.provenanceAvailable && offlineSafe
        val reason = when {
            !offlineSafe -> "Offline-Kaskade oder begrenzter Cache fehlt; Gateway BLOCKED."
            !networkAllowed -> "Netzwerkzugriff verweigert: Anonymisierung, Policy oder Einwilligung fehlt."
            !evidence.provenanceAvailable -> "Quellenprovenienz fehlt; Antwort darf nicht als abgerufen markiert werden."
            else -> "Anonymisiertes Wissensgateway ist freigegeben."
        }
        return KnowledgeGatewayAdmissionResult(networkAllowed, offlineSafe, ready, reason)
    }
}
