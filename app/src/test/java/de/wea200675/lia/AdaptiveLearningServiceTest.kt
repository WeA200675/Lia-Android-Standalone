package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertFalse(store.get("lia.learning.feedback.v1")!!.toString(Charsets.UTF_8).contains("0123456789"))
    }

    @Test fun ignoresDuplicatesAndOversizedFeedback() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        service.record(LearningFeedbackSignal("Hilfreich", true))
        val duplicate = service.record(LearningFeedbackSignal("  HILFREICH ", true))
        assertEquals(1, duplicate.acceptedSignals)
        val oversized = service.record(LearningFeedbackSignal("x".repeat(501), true))
        assertEquals(1, oversized.acceptedSignals)
    }

    @Test fun approvedFeedbackIsAvailableAsBoundedPromptContext() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        service.record(LearningFeedbackSignal("Kritik: meine Mail test@example.org war falsch", positive = false))
        val context = service.recentFeedback()
        assertEquals(1, context.size)
        assertTrue(context.single().contains("[E-MAIL]"))
        assertFalse(context.single().contains("test@example.org"))
    }

    @Test fun resetRemovesHistoryAndEvolution() {
        val store = MemoryStore()
        val service = AdaptiveLearningService(LearningEvolutionStore(store), store)
        service.record(LearningFeedbackSignal("Gut", true))
        service.reset()
        assertEquals(1, service.record(LearningFeedbackSignal("Neu", true)).acceptedSignals)
    }
}
