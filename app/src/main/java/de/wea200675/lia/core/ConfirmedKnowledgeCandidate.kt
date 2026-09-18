package de.wea200675.lia.core

/** A retainable answer exists only when local AI used traceable external knowledge. */
data class ConfirmedKnowledgeCandidate(
    val fingerprint: String,
    val summary: String,
    val sourceLabels: List<String>
) {
    companion object {
        fun from(question: String, answer: OrchestratedAnswer): ConfirmedKnowledgeCandidate? {
            if (answer.source != AnswerSource.LOCAL_AI || !answer.webContextUsed) return null
            val provenance = answer.knowledgeProvenance ?: return null
            val labels = provenance.sourceLabels.map(String::trim).filter(String::isNotEmpty).distinct().take(5)
            val summary = UntrustedKnowledgeBoundary.sanitize(answer.text) ?: return null
            if (question.isBlank() || labels.isEmpty()) return null
            return ConfirmedKnowledgeCandidate(
                fingerprint = ConfirmedKnowledgeRepository.fingerprint(Anonymizer.redact(question)),
                summary = summary,
                sourceLabels = labels
            )
        }
    }
}
