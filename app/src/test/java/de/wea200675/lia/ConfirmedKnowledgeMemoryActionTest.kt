package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmedKnowledgeMemoryActionTest {
    private class MemoryStore : SecureStore {
        val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value }
        override fun get(key: String): ByteArray? = values[key]
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun missingOrUnprovenancedAnswerCannotBeStoredAsPersonalMemory() {
        val store = MemoryStore()
        val action = ConfirmedKnowledgeMemoryAction(ConfirmedKnowledgeRepository(store))
        assertFalse(action.save(null))
        assertTrue(store.values.isEmpty())
    }

    @Test fun onlyAnExplicitTraceableCandidateIsSaved() {
        val store = MemoryStore()
        val action = ConfirmedKnowledgeMemoryAction(ConfirmedKnowledgeRepository(store))
        val candidate = ConfirmedKnowledgeCandidate(
            fingerprint = "a".repeat(64),
            summary = "Validierte Information.",
            sourceLabels = listOf("Quelle")
        )
        assertTrue(action.save(candidate))
        assertTrue(ConfirmedKnowledgeRepository(store).all().single().summary.contains("Validierte"))
    }
}
