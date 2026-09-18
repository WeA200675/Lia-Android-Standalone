package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeSourceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSourceCatalogTest {
    @Test fun catalogContainsExactlySeventeenAuditedHosts() {
        val expectedHosts = setOf(
            "de.wikipedia.org", "en.wikipedia.org", "de.wiktionary.org",
            "de.wikibooks.org", "de.wikivoyage.org", "de.wikisource.org",
            "www.wikidata.org", "commons.wikimedia.org", "de.wikinews.org",
            "de.wikiquote.org", "de.wikiversity.org", "species.wikimedia.org",
            "www.mediawiki.org", "meta.wikimedia.org", "www.wikifunctions.org",
            "en.wikibooks.org", "en.wikivoyage.org"
        )
        assertEquals(17, KnowledgeSourceCatalog.sources.size)
        assertEquals(expectedHosts, KnowledgeSourceCatalog.sources.map { it.host }.toSet())
        assertFalse(KnowledgeSourceCatalog.isAllowed("example.org"))
    }

    @Test fun definitionPrefersWiktionaryThenGeneralFallbacks() {
        val selected = KnowledgeSourceCatalog.select("Was bedeutet Photosynthese?")
        assertEquals("wiktionary-de", selected.first().id)
        assertTrue(selected.any { it.id == "wikipedia-de" })
        assertTrue(selected.size <= 3)
    }

    @Test fun travelAndLearningUseSpecializedSources() {
        assertEquals("wikivoyage-de", KnowledgeSourceCatalog.select("Reise nach Hamburg").first().id)
        assertEquals("wikibooks-de", KnowledgeSourceCatalog.select("Anleitung zum Zeichnen").first().id)
    }

    @Test fun addedTopicsRouteToTheirSpecializedKnowledgeBase() {
        assertEquals("wikiquote-de", KnowledgeSourceCatalog.select("Wer sagte dieses Zitat?").first().id)
        assertEquals("wikinews-de", KnowledgeSourceCatalog.select("Welche Nachricht ist aktuell?").first().id)
        assertEquals("wikiversity-de", KnowledgeSourceCatalog.select("Eine Lektion als Lernmaterial").first().id)
        assertEquals("wikispecies", KnowledgeSourceCatalog.select("Welche Tierart und Gattung?").first().id)
        assertEquals("commons", KnowledgeSourceCatalog.select("Gibt es ein Bild davon?").first().id)
        assertEquals("mediawiki", KnowledgeSourceCatalog.select("MediaWiki installieren").first().id)
        assertEquals("meta-wiki", KnowledgeSourceCatalog.select("Wikimedia Foundation Community").first().id)
        assertEquals("wikifunctions", KnowledgeSourceCatalog.select("Algorithmus und Berechnung").first().id)
    }

    @Test fun genericQuestionUsesGermanWikipediaFirst() {
        val selected = KnowledgeSourceCatalog.select("Warum ist der Himmel blau?")
        assertEquals("wikipedia-de", selected.first().id)
        assertEquals(selected.distinctBy { it.id }.size, selected.size)
    }
}
