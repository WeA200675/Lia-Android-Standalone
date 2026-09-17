package de.wea200675.lia.core

/** Encrypted, bounded persistence for the assistant's knowledge facts. */
class KnowledgeStore(private val secureStore: SecureStore, private val key: String = "lia.knowledge.v1") {
    fun load(): List<KnowledgeEntry> {
        val raw = secureStore.get(key)?.toString(Charsets.UTF_8) ?: return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val p = line.split("|", limit = 7)
            if (p.size != 7) null else KnowledgeEntry(
                id = p[0], summary = p[1], source = p[2],
                createdAt = p[3].toLongOrNull() ?: 0L,
                lastUsedAt = p[4].toLongOrNull() ?: 0L,
                confidence = p[5].toIntOrNull() ?: 0,
                personal = p[6] == "1"
            )
        }.toList()
    }

    fun save(entries: List<KnowledgeEntry>) {
        val bounded = entries.sortedByDescending { it.confidence }.take(MAX_ENTRIES)
        val raw = bounded.joinToString("\n") {
            listOf(it.id, it.summary.replace("|", " "), it.source.replace("|", " "),
                it.createdAt, it.lastUsedAt, it.confidence, if (it.personal) "1" else "0").joinToString("|")
        }
        secureStore.put(key, raw.toByteArray(Charsets.UTF_8))
    }

    fun clear() = secureStore.delete(key)

    companion object { const val MAX_ENTRIES = 500 }
}
