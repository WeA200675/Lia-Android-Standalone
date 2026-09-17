package de.wea200675.lia.core

data class KnowledgeEntry(
    val id: String,
    val summary: String,
    val source: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = createdAt,
    val confidence: Int = 50,
    val personal: Boolean = false,
    val archived: Boolean = false
)

class KnowledgeManager {
    fun upsert(entries: List<KnowledgeEntry>, incoming: KnowledgeEntry): List<KnowledgeEntry> {
        val match = entries.firstOrNull { it.summary.trim().equals(incoming.summary.trim(), ignoreCase = true) }
        return if (match == null) entries + incoming
        else entries.map { if (it.id == match.id) it.copy(lastUsedAt = maxOf(it.lastUsedAt, incoming.lastUsedAt), confidence = maxOf(it.confidence, incoming.confidence)) else it }
    }

    fun archiveStale(entries: List<KnowledgeEntry>, now: Long = System.currentTimeMillis(), maxAgeDays: Long = 180): List<KnowledgeEntry> {
        val cutoff = now - maxAgeDays * 86_400_000L
        return entries.map { if (!it.personal && it.lastUsedAt < cutoff) it.copy(archived = true) else it }
    }

    fun active(entries: List<KnowledgeEntry>): List<KnowledgeEntry> = entries.filterNot { it.archived }
}
