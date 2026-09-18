package de.wea200675.lia.core

data class KnowledgeRuntimeSnapshot(
    val cachedEntries: Int,
    val cacheCapacity: Int,
    val sourcesWithFailures: Int,
    val suspendedSources: Int
)

/** Process-local shared state for runtime diagnostics and PIN-protected reset. */
object KnowledgeSessionRuntime {
    private var cache = BoundedKnowledgeCache()
    val sourceHealth = KnowledgeSourceHealthTracker()

    @Synchronized
    fun configureCache(maxEntries: Int): BoundedKnowledgeCache {
        if (cache.capacity() != maxEntries) cache = BoundedKnowledgeCache(maxEntries)
        return cache
    }

    @Synchronized
    fun cache(): BoundedKnowledgeCache = cache

    @Synchronized
    fun snapshot(): KnowledgeRuntimeSnapshot = KnowledgeRuntimeSnapshot(
        cachedEntries = cache.size(),
        cacheCapacity = cache.capacity(),
        sourcesWithFailures = sourceHealth.sourcesWithFailures(),
        suspendedSources = sourceHealth.suspendedCount()
    )

    @Synchronized
    fun reset() {
        cache.clear()
        sourceHealth.reset()
    }
}
