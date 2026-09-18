package de.wea200675.lia.core

import java.time.LocalDate

/** Encrypted cursor that prevents handled daily prompts from repeating after an app restart. */
class DailyPromptProgress(
    private val secureStore: SecureStore,
    private val key: String = "lia.training.progress.v1"
) {
    fun nextIndex(date: LocalDate, promptCount: Int): Int {
        require(promptCount >= 0)
        val raw = runCatching { secureStore.get(key)?.toString(Charsets.UTF_8) }.getOrNull()
            ?: return 0
        val parts = raw.split("|", limit = 2)
        if (parts.size != 2 || parts[0] != date.toString()) return 0
        return parts[1].toIntOrNull()?.coerceIn(0, promptCount) ?: 0
    }

    fun markHandled(date: LocalDate, currentIndex: Int, promptCount: Int): Int {
        require(promptCount >= 0)
        val next = (currentIndex + 1).coerceIn(0, promptCount)
        runCatching {
            secureStore.put(key, "${date}|${next}".toByteArray(Charsets.UTF_8))
        }
        return next
    }

    fun clear() = secureStore.delete(key)
}
