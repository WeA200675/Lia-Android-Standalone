package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveLearningServiceTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value }
        override fun get(key: String): ByteArray? = values[key]
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun recordsAnonymizedFeedbackAndAdvancesEvolution() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        val state = service.record(LearningFeedbackSignal("Meine Telefonnummer ist 0123456789, das war hilfreich.", true))
        assertEquals(1, state.acceptedSignals)
        assertEquals(DevelopmentStage.FOUNDATION, state.stage)
        assertEquals("meine telefonnummer ist [redacted], das war hilfreich.", store.get("lia.learning.feedback.v1")!!.toString(Charsets.UTF_8))
    }

    @Test fun ignoresDuplicatesAndOversizedFeedback() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        service.record(FeedbackSignal("Hilfreich", true))
        val duplicate = service.record(FeedbackSignal("  HILFREICH ", true))
        assertEquals(1, duplicate.acceptedSignals)
        val oversized = service.record(FeedbackSignal("x".repeat(501), true))
        assertEquals(1, oversized.acceptedSignals)
    }

    @Test fun resetRemovesHistoryAndEvolution() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        service.record(FeedbackSignal("Gut", true))
        service.reset()
        assertEquals(0, service.record(FeedbackSignal("Neu", true)).acceptedSignals)
    }
}
