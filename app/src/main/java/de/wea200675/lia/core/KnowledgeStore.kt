package de.wea200675.lia.core

/** Encrypted, bounded persistence for the assistant's non-sensitive knowledge facts. */
class KnowledgeStore(private val secureStore: SecureStore, private val key: String = "lia.knowledge.v1") {
    fun load(): List<KnowledgeEntry> {
        val raw = secureStore.get(key)?.toString(Charsets.UTF_8) ?: return emptyList()
        return raw.lineSequence().mapNotNull { line ->
            val p = line.split("|", limit = 5)
            if (p.size != 5) null else KnowledgeEntry(
                id = p[0], text = p[1], confidence = p[2].toDoubleOrNull() ?: 0.0,
                source = p[3], updatedEpochDay = p[4].toLongOrNull() ?: 0L
            )
        }.toList()
    }

    fun save(entries: List<KnowledgeEntry>) {
        val bounded = entries.sortedByDescending { it.confidence }.take(MAX_ENTRIES)
        val raw = bounded.joinToString("\n") {
            listOf(it.id, it.text.replace("|", " "), it.confidence.toString(), it.source, it.updatedEpochDay.toString()).joinToString("|")
        }
        secureStore.put(key, raw.toByteArray(Charsets.UTF_8))
    }

    fun clear() = secureStore.delete(key)

    companion object { const val MAX_ENTRIES = 500 }
}
