package de.wea200675.lia

import de.wea200675.lia.core.RestartBudgetStore
import de.wea200675.lia.core.SecureStore
import org.junit.Assert.assertEquals
import org.junit.Test

class RestartBudgetStoreTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value.copyOf() }
        override fun get(key: String): ByteArray? = values[key]?.copyOf()
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun valuesAreClampedToSafeRangeAndPersisted() {
        val store = MemoryStore()
        val settings = RestartBudgetStore(store)
        assertEquals(RestartBudgetStore.MIN, settings.save(-4))
        assertEquals(RestartBudgetStore.MIN, RestartBudgetStore(store).load())
        assertEquals(RestartBudgetStore.MAX, settings.save(99))
        assertEquals(RestartBudgetStore.MAX, RestartBudgetStore(store).load())
    }

    @Test fun malformedStoredValueUsesDefault() {
        val store = MemoryStore()
        store.put("lia.runtime.restart-budget.v1", "not-a-number".toByteArray())
        assertEquals(RestartBudgetStore.DEFAULT, RestartBudgetStore(store).load())
    }
}
