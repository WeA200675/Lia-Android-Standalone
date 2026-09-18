package de.wea200675.lia.core

enum class KnowledgeTopic {
    GENERAL, DEFINITION, TRAVEL, LEARNING, HISTORY, STRUCTURED,
    QUOTE, NEWS, EDUCATION, SPECIES, MEDIA, TECHNICAL, COMMUNITY, COMPUTING
}

data class KnowledgeSource(
    val id: String,
    val label: String,
    val host: String,
    val topic: KnowledgeTopic,
    val language: String
)

/**
 * Auditable allowlist. Every source is read-only, account-free and operated
 * within the Wikimedia ecosystem. Selection is deterministic and bounded.
 */
object KnowledgeSourceCatalog {
    val sources = listOf(
        KnowledgeSource("wikipedia-de", "Wikipedia (Deutsch)", "de.wikipedia.org", KnowledgeTopic.GENERAL, "de"),
        KnowledgeSource("wikipedia-en", "Wikipedia (Englisch)", "en.wikipedia.org", KnowledgeTopic.GENERAL, "en"),
        KnowledgeSource("wiktionary-de", "Wiktionary (Deutsch)", "de.wiktionary.org", KnowledgeTopic.DEFINITION, "de"),
        KnowledgeSource("wikibooks-de", "Wikibooks (Deutsch)", "de.wikibooks.org", KnowledgeTopic.LEARNING, "de"),
        KnowledgeSource("wikivoyage-de", "Wikivoyage (Deutsch)", "de.wikivoyage.org", KnowledgeTopic.TRAVEL, "de"),
        KnowledgeSource("wikisource-de", "Wikisource (Deutsch)", "de.wikisource.org", KnowledgeTopic.HISTORY, "de"),
        KnowledgeSource("wikidata", "Wikidata", "www.wikidata.org", KnowledgeTopic.STRUCTURED, "mul"),

        KnowledgeSource("commons", "Wikimedia Commons", "commons.wikimedia.org", KnowledgeTopic.MEDIA, "mul"),
        KnowledgeSource("wikinews-de", "Wikinews (Deutsch)", "de.wikinews.org", KnowledgeTopic.NEWS, "de"),
        KnowledgeSource("wikiquote-de", "Wikiquote (Deutsch)", "de.wikiquote.org", KnowledgeTopic.QUOTE, "de"),
        KnowledgeSource("wikiversity-de", "Wikiversity (Deutsch)", "de.wikiversity.org", KnowledgeTopic.EDUCATION, "de"),
        KnowledgeSource("wikispecies", "Wikispecies", "species.wikimedia.org", KnowledgeTopic.SPECIES, "mul"),
        KnowledgeSource("mediawiki", "MediaWiki Dokumentation", "www.mediawiki.org", KnowledgeTopic.TECHNICAL, "mul"),
        KnowledgeSource("meta-wiki", "Wikimedia Meta-Wiki", "meta.wikimedia.org", KnowledgeTopic.COMMUNITY, "mul"),
        KnowledgeSource("wikifunctions", "Wikifunctions", "www.wikifunctions.org", KnowledgeTopic.COMPUTING, "mul"),
        KnowledgeSource("wikibooks-en", "Wikibooks (Englisch)", "en.wikibooks.org", KnowledgeTopic.LEARNING, "en"),
        KnowledgeSource("wikivoyage-en", "Wikivoyage (Englisch)", "en.wikivoyage.org", KnowledgeTopic.TRAVEL, "en")
    )

    private val rules = listOf(
        KnowledgeTopic.DEFINITION to Regex("""\b(was bedeutet|bedeutung|definition|wort|übersetze)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.TRAVEL to Regex("""\b(reise|reiseziel|urlaub|sehenswürdigkeit|wandern|hotel|stadtführung)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.LEARNING to Regex("""\b(anleitung|lernen|erkläre schritt|wie baue|wie mache|lehrbuch)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.HISTORY to Regex("""\b(quelle|originaltext|historisch|gedicht|rede|handschrift)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.STRUCTURED to Regex("""\b(geboren|gestorben|einwohner|hauptstadt|höhe|datum|koordinaten)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.QUOTE to Regex("""\b(zitat|spruch|ausspruch|wer sagte)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.NEWS to Regex("""\b(nachricht|neuigkeit|aktuell|heute passiert)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.EDUCATION to Regex("""\b(kurs|lektion|lernmaterial|übung|studium)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.SPECIES to Regex("""\b(tierart|pflanzenart|spezies|gattung|familie|lateinischer name)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.MEDIA to Regex("""\b(bild|foto|aufnahme|karte|audio|video)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.TECHNICAL to Regex("""\b(mediawiki|wiki software|wiki installieren)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.COMMUNITY to Regex("""\b(wikimedia|wiki projekt|community|foundation)\b""", RegexOption.IGNORE_CASE),
        KnowledgeTopic.COMPUTING to Regex("""\b(funktion|berechnung|algorithmus|programmierung)\b""", RegexOption.IGNORE_CASE)
    )

    fun select(query: String, limit: Int = 3): List<KnowledgeSource> {
        val preferred = rules.firstOrNull { it.second.containsMatchIn(query) }?.first
            ?: KnowledgeTopic.GENERAL
        val ordered = sources.filter { it.topic == preferred } +
            sources.filter { it.id == "wikipedia-de" } +
            sources.filter { it.id == "wikidata" } +
            sources.filter { it.topic == KnowledgeTopic.GENERAL } +
            sources
        return ordered.distinctBy { it.id }.take(limit.coerceIn(1, 3))
    }

    fun isAllowed(host: String): Boolean = sources.any { it.host == host }
}
