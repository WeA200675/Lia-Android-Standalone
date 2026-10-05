package de.wea200675.lia.core

/**
 * The UI can retain only explicitly confirmed, source-traceable knowledge.
 * Personal memories are collected through the separate daily-answer flow.
 */
class ConfirmedKnowledgeMemoryAction(
    private val repository: ConfirmedKnowledgeRepository
) {
    fun save(candidate: ConfirmedKnowledgeCandidate?): Boolean {
        candidate ?: return false
        return repository.saveConfirmed(
            candidate.summary,
            candidate.sourceLabels,
            candidate.fingerprint
        )
    }
}
