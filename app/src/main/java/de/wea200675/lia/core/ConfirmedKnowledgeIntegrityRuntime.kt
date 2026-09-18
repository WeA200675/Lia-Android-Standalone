package de.wea200675.lia.core

enum class KnowledgeIntegrityState { HEALTHY, SECURITY_FAILURE }

data class KnowledgeIntegritySnapshot(
    val state: KnowledgeIntegrityState,
    val detectedAtEpochMs: Long?
)

/** Process-local alarm; no sensitive exception details are persisted. */
object ConfirmedKnowledgeIntegrityRuntime {
    @Volatile private var state = KnowledgeIntegrityState.HEALTHY
    @Volatile private var detectedAtEpochMs: Long? = null

    fun recordSecurityFailure(nowEpochMs: Long = System.currentTimeMillis()) {
        state = KnowledgeIntegrityState.SECURITY_FAILURE
        if (detectedAtEpochMs == null) detectedAtEpochMs = nowEpochMs
    }

    fun snapshot(): KnowledgeIntegritySnapshot = KnowledgeIntegritySnapshot(state, detectedAtEpochMs)

    /** Call only after the encrypted confirmed-knowledge payload was deleted by an unlocked admin. */
    fun resetAfterDeletion() {
        state = KnowledgeIntegrityState.HEALTHY
        detectedAtEpochMs = null
    }
}
