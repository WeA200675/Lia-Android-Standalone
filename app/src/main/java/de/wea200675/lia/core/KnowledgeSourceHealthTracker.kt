package de.wea200675.lia.core

data class SourceHealth(
    val consecutiveFailures: Int,
    val suspendedUntilMs: Long
)

/**
 * Session-only circuit breaker for external sources. It never permanently
 * disables a source: after cooldown exactly the normal request path probes it.
 */
class KnowledgeSourceHealthTracker(
    private val failureThreshold: Int = 3,
    private val cooldownMs: Long = 15 * 60_000L,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val health = mutableMapOf<String, SourceHealth>()

    init {
        require(failureThreshold in 1..10)
        require(cooldownMs in 60_000L..(24 * 60 * 60_000L))
    }

    @Synchronized
    fun canAttempt(sourceId: String): Boolean {
        val current = health[sourceId] ?: return true
        return current.suspendedUntilMs <= clock()
    }

    @Synchronized
    fun recordSuccess(sourceId: String) {
        health.remove(sourceId)
    }

    @Synchronized
    fun recordFailure(sourceId: String) {
        val previous = health[sourceId] ?: SourceHealth(0, 0)
        val failures = (previous.consecutiveFailures + 1).coerceAtMost(failureThreshold)
        val suspendedUntil = if (failures >= failureThreshold) clock() + cooldownMs else 0L
        health[sourceId] = SourceHealth(failures, suspendedUntil)
    }

    @Synchronized
    fun status(sourceId: String): SourceHealth = health[sourceId] ?: SourceHealth(0, 0)

    @Synchronized
    fun reset() = health.clear()
}
