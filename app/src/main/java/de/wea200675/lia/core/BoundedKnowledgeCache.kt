package de.wea200675.lia.core

import java.security.MessageDigest
import java.util.LinkedHashMap

object KnowledgeFreshnessPolicy {
    const val MINUTE_MS = 60_000L
    const val HOUR_MS = 60 * MINUTE_MS
    const val DAY_MS = 24 * HOUR_MS

    fun ttlMillis(query: String): Long = when (KnowledgeSourceCatalog.select(query).firstOrNull()?.topic) {
        KnowledgeTopic.NEWS -> 15 * MINUTE_MS
        KnowledgeTopic.TRAVEL -> 6 * HOUR_MS
        KnowledgeTopic.TECHNICAL, KnowledgeTopic.COMPUTING -> 12 * HOUR_MS
        KnowledgeTopic.DEFINITION, KnowledgeTopic.HISTORY, KnowledgeTopic.QUOTE -> 7 * DAY_MS
        else -> DAY_MS
    }
}

/**
 * Small session-only LRU cache. Keys are one-way hashes, entries expire, and
 * no cache data survives an app process restart.
 */
class BoundedKnowledgeCache(
    private val maxEntries: Int = 32,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private data class Entry(val value: String, val expiresAt: Long)
    private val entries = LinkedHashMap<String, Entry>(16, 0.75f, true)

    @Synchronized
    fun get(query: String): String? {
        purgeExpired()
        return entries[key(query)]?.value
    }

    @Synchronized
    fun put(query: String, value: String, ttlMillis: Long) {
        require(maxEntries in 1..128)
        require(ttlMillis in KnowledgeFreshnessPolicy.MINUTE_MS..(30 * KnowledgeFreshnessPolicy.DAY_MS))
        val bounded = value.trim().take(MAX_VALUE_CHARS)
        if (bounded.isEmpty()) return
        purgeExpired()
        entries[key(query)] = Entry(bounded, clock() + ttlMillis)
        while (entries.size > maxEntries) {
            entries.remove(entries.entries.first().key)
        }
    }

    @Synchronized
    fun clear() = entries.clear()

    @Synchronized
    fun size(): Int {
        purgeExpired()
        return entries.size
    }

    private fun purgeExpired() {
        val now = clock()
        entries.entries.removeAll { it.value.expiresAt <= now }
    }

    private fun key(query: String): String {
        val normalized = query.trim().lowercase().replace(Regex("""\s+"""), " ")
        return MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val MAX_VALUE_CHARS = 4_000
    }
}
