package de.wea200675.lia

import de.wea200675.lia.core.DailyPromptProgress
import de.wea200675.lia.core.SecureStore
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DailyPromptProgressTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value.copyOf() }
        override fun get(key: String): ByteArray? = values[key]?.copyOf()
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun handledPromptIsNotRepeatedAfterRestart() {
        val store = MemoryStore()
        val date = LocalDate.of(2026, 9, 18)
        val progress = DailyPromptProgress(store)

        assertEquals(0, progress.nextIndex(date, 25))
        assertEquals(1, progress.markHandled(date, 0, 25))
        assertEquals(1, DailyPromptProgress(store).nextIndex(date, 25))
    }

    @Test fun newDayStartsAtFirstPrompt() {
        val store = MemoryStore()
        val firstDay = LocalDate.of(2026, 9, 18)
        val progress = DailyPromptProgress(store)
        progress.markHandled(firstDay, 7, 25)

        assertEquals(0, progress.nextIndex(firstDay.plusDays(1), 25))
    }

    @Test fun completionNeverWrapsToFirstPrompt() {
        val store = MemoryStore()
        val date = LocalDate.of(2026, 9, 18)
        val progress = DailyPromptProgress(store)

        assertEquals(25, progress.markHandled(date, 24, 25))
        assertEquals(25, progress.markHandled(date, 25, 25))
        assertEquals(25, progress.nextIndex(date, 25))
    }
}
