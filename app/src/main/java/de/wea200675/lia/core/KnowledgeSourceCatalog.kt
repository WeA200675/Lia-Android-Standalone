package de.wea200675.lia.core

enum class KnowledgeTopic { GENERAL, DEFINITION, TRAVEL, LEARNING, HISTORY, STRUCTURED }

data class KnowledgeSource(
    val id: String,
    val label: String,
    val host: String,
    val topic: KnowledgeTopic,
    val language: String
)

/**
 * Auditable allowlist. Every source is read-only, account-free and served by
 * the Wikimedia Foundation. Selection is deterministic and limited per query.
 */
object KnowledgeSourceCatalog {
    val sources = listOf(
        KnowledgeSource("wikipedia-de", "Wikipedia (Deutsch)", "de.wikipedia.org", KnowledgeTopic.GENERAL, "de"),
        KnowledgeSource("wikipedia-en", "Wikipedia (Englisch)", "en.wikipedia.org", KnowledgeTopic.GENERAL, "en"),
        KnowledgeSource("wiktionary-de", "Wiktionary (Deutsch)", "de.wiktionary.org", KnowledgeTopic.DEFINITION, "de"),
        KnowledgeSource("wikibooks-de", "Wikibooks (Deutsch)", "de.wikibooks.org", KnowledgeTopic.LEARNING, "de"),
        KnowledgeSource("wikivoyage-de", "Wikivoyage (Deutsch)", "de.wikivoyage.org", KnowledgeTopic.TRAVEL, "de"),
        KnowledgeSource("wikisource-de", "Wikisource (Deutsch)", "de.wikisource.org", KnowledgeTopic.HISTORY, "de"),
        KnowledgeSource("wikidata", "Wikidata", "www.wikidata.org", KnowledgeTopic.STRUCTURED, "mul")
    )

    private val definitionWords = Regex("""\b(was bedeutet|bedeutung|definition|wort|übersetze)\b""", RegexOption.IGNORE_CASE)
    private val travelWords = Regex("""\b(reise|reiseziel|urlaub|sehenswürdigkeit|wandern|hotel|stadtführung)\b""", RegexOption.IGNORE_CASE)
    private val learningWords = Regex("""\b(anleitung|lernen|erkläre schritt|wie baue|wie mache|lehrbuch)\b""", RegexOption.IGNORE_CASE)
    private val historyWords = Regex("""\b(quelle|originaltext|historisch|gedicht|rede|handschrift)\b""", RegexOption.IGNORE_CASE)
    private val structuredWords = Regex("""\b(geboren|gestorben|einwohner|hauptstadt|höhe|datum|koordinaten)\b""", RegexOption.IGNORE_CASE)

    fun select(query: String, limit: Int = 3): List<KnowledgeSource> {
        val preferred = when {
            definitionWords.containsMatchIn(query) -> KnowledgeTopic.DEFINITION
            travelWords.containsMatchIn(query) -> KnowledgeTopic.TRAVEL
            learningWords.containsMatchIn(query) -> KnowledgeTopic.LEARNING
            historyWords.containsMatchIn(query) -> KnowledgeTopic.HISTORY
            structuredWords.containsMatchIn(query) -> KnowledgeTopic.STRUCTURED
            else -> KnowledgeTopic.GENERAL
        }
        val ordered = sources.filter { it.topic == preferred } +
            sources.filter { it.id == "wikipedia-de" } +
            sources.filter { it.id == "wikidata" } +
            sources.filter { it.topic == KnowledgeTopic.GENERAL } +
            sources
        return ordered.distinctBy { it.id }.take(limit.coerceIn(1, 3))
    }

    fun isAllowed(host: String): Boolean = sources.any { it.host == host }
}
