package de.wea200675.lia

import de.wea200675.lia.core.KnowledgeOrigin
import de.wea200675.lia.core.KnowledgePresentation
import de.wea200675.lia.core.KnowledgeProvenance
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgePresentationTest {
    private fun p(origin: KnowledgeOrigin) = KnowledgeProvenance(listOf("InternalGatewayName", "timestamp"), origin, 123L)

    @Test fun liveKnowledgeUsesPlainPrivacySafeLabel() {
        val line = KnowledgePresentation.sourceLine(p(KnowledgeOrigin.LIVE))
        assertTrue(line.contains("anonymisierte Anfrage"))
        assertFalse(line.contains("InternalGatewayName"))
        assertFalse(line.contains("123"))
    }

    @Test fun localAndCachedKnowledgeAreClearlySeparated() {
        val local = KnowledgePresentation.sourceLine(p(KnowledgeOrigin.CONFIRMED_STORE))
        val cached = KnowledgePresentation.sourceLine(p(KnowledgeOrigin.SESSION_CACHE))
        assertTrue(local.contains("bestätigtem lokalem Wissen"))
        assertTrue(cached.contains("Sitzungspuffer"))
        assertFalse(local == cached)
    }

    @Test fun offlineWithoutProvenanceHasNoSourceClaim() {
        assertTrue(KnowledgePresentation.sourceLine(null).isEmpty())
    }
}
