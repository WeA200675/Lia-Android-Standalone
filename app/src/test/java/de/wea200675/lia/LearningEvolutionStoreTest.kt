package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningEvolutionStoreTest {
    private class MemoryStore : SecureStore {
        private var value: ByteArray? = null
        override fun put(key: String, value: ByteArray) { this.value = value }
        override fun get(key: String): ByteArray? = value
        override fun delete(key: String) { value = null }
    }

    @Test fun persistsEvolutionStateThroughSecureStore() {
        val store = LearningEvolutionStore(MemoryStore())
        val state = LearningEvolutionState(DevelopmentStage.EMPATHY, 42, 3, setOf("feedback", "interest"))
        store.save(state)
        assertEquals(state, store.load())
    }

    @Test fun corruptedValuesFallBackSafely() {
        val memory = MemoryStore()
        val store = LearningEvolutionStore(memory)
        memory.put("x", "not-a-valid-state".toByteArray())
        assertEquals(DevelopmentStage.FOUNDATION, store.load().stage)
        assertEquals(0, store.load().acceptedSignals)
    }

    @Test fun resetRemovesEvolutionState() {
        val store = LearningEvolutionStore(MemoryStore())
        store.save(LearningEvolutionState(DevelopmentStage.DIALOGUE, 100, 0, emptySet()))
        store.reset()
        assertEquals(DevelopmentStage.FOUNDATION, store.load().stage)
    }
}
