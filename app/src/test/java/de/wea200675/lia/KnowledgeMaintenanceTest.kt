package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeMaintenanceTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value }
        override fun get(key: String): ByteArray? = values[key]
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun maintenanceIsIdempotentAndReportsBoundedStorage() {
        val repo = ConfirmedKnowledgeRepository(MemoryStore(), maxEntries = 2, clock = { 1_000_000L })
        assertTrue(repo.saveConfirmed("Kurze bestätigte Information", listOf("Quelle"), "a".repeat(64), ttlMs = ConfirmedKnowledgeRepository.MIN_TTL_MS))
        val first = repo.maintain()
        val second = repo.maintain()
        assertEquals(first, second)
        assertEquals(1, second.keptEntries)
        assertTrue(second.serializedBytes <= second.maxBytes)
        assertTrue(second.keptEntries <= second.maxEntries)
    }
}
