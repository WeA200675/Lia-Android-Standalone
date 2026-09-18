package de.wea200675.lia

import de.wea200675.lia.core.BoundedKnowledgeCache
import de.wea200675.lia.core.KnowledgeFreshnessPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundedKnowledgeCacheTest {
    @Test fun reusesFreshEntryAndNormalizesQueryKey() {
        var now = 1_000L
        val cache = BoundedKnowledgeCache(clock = { now })
        cache.put("Warum ist der Himmel blau?", "Antwort", KnowledgeFreshnessPolicy.HOUR_MS)

        assertEquals("Antwort", cache.get("  WARUM   ist der Himmel blau? "))
        assertEquals(1, cache.size())
    }

    @Test fun expiredEntryIsRemoved() {
        var now = 10_000L
        val cache = BoundedKnowledgeCache(clock = { now })
        cache.put("Frage", "Antwort", KnowledgeFreshnessPolicy.MINUTE_MS)
        now += KnowledgeFreshnessPolicy.MINUTE_MS

        assertNull(cache.get("Frage"))
        assertEquals(0, cache.size())
    }

    @Test fun leastRecentlyUsedEntriesAreEvicted() {
        val cache = BoundedKnowledgeCache(maxEntries = 2, clock = { 1_000L })
        cache.put("eins", "1", KnowledgeFreshnessPolicy.HOUR_MS)
        cache.put("zwei", "2", KnowledgeFreshnessPolicy.HOUR_MS)
        assertEquals("1", cache.get("eins"))
        cache.put("drei", "3", KnowledgeFreshnessPolicy.HOUR_MS)

        assertNull(cache.get("zwei"))
        assertEquals("1", cache.get("eins"))
        assertEquals("3", cache.get("drei"))
    }

    @Test fun cacheNeverGrowsBeyondConfiguredLimit() {
        val cache = BoundedKnowledgeCache(maxEntries = 32, clock = { 1_000L })
        repeat(100) { cache.put("Frage $it", "Antwort $it", KnowledgeFreshnessPolicy.DAY_MS) }
        assertEquals(32, cache.size())
    }

    @Test fun freshnessMatchesVolatility() {
        val news = KnowledgeFreshnessPolicy.ttlMillis("Welche Nachricht ist aktuell?")
        val history = KnowledgeFreshnessPolicy.ttlMillis("Zeige eine historische Quelle")
        assertTrue(news < history)
        assertEquals(15 * KnowledgeFreshnessPolicy.MINUTE_MS, news)
        assertEquals(7 * KnowledgeFreshnessPolicy.DAY_MS, history)
    }
}
