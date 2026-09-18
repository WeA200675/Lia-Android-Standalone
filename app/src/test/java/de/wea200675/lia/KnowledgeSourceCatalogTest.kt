package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeSourceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSourceCatalogTest {
    @Test fun catalogContainsOnlyExplicitWikimediaAllowlist() {
        assertEquals(7, KnowledgeSourceCatalog.sources.size)
        assertTrue(KnowledgeSourceCatalog.sources.all {
            it.host.endsWith(".wikipedia.org") ||
                it.host.endsWith(".wiktionary.org") ||
                it.host.endsWith(".wikibooks.org") ||
                it.host.endsWith(".wikivoyage.org") ||
                it.host.endsWith(".wikisource.org") ||
                it.host == "www.wikidata.org"
        })
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

    @Test fun genericQuestionUsesGermanWikipediaFirst() {
        val selected = KnowledgeSourceCatalog.select("Warum ist der Himmel blau?")
        assertEquals("wikipedia-de", selected.first().id)
        assertEquals(selected.distinctBy { it.id }.size, selected.size)
    }
}
