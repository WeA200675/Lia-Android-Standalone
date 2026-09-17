package de.wea200675.lia

import de.wea200675.lia.core.DailyPlanRepository
import de.wea200675.lia.core.DailyTrainingPlanner
import de.wea200675.lia.core.SecureStore
import de.wea200675.lia.core.TrainingCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyPlanRepositoryTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value.copyOf() }
        override fun get(key: String): ByteArray? = values[key]?.copyOf()
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun sameDateReusesPersistedPlan() {
        val date = LocalDate.of(2026, 9, 18)
        val store = MemoryStore()
        val cache = TrainingCache(store)
        val original = DailyTrainingPlanner.forDate(date)
        cache.save(original)
        var plannerCalls = 0
        val repository = DailyPlanRepository(cache) {
            plannerCalls += 1
            DailyTrainingPlanner.forDate(it)
        }

        assertEquals(original, repository.forDate(date))
        assertEquals(0, plannerCalls)
    }

    @Test fun newDateCreatesAndPersistsExactlyTwentyFivePrompts() {
        val date = LocalDate.of(2026, 9, 19)
        val store = MemoryStore()
        val cache = TrainingCache(store)
        val plan = DailyPlanRepository(cache).forDate(date)

        assertEquals(date, plan.date)
        assertEquals(25, plan.prompts.size)
        assertTrue(plan.prompts.all { it.optional })
        assertEquals(plan, cache.load())
    }
}
