package de.wea200675.lia.core

import java.time.LocalDate

class TrainingCache(private val secureStore: SecureStore, private val key: String = "lia.training.cache.v1") {
    fun save(plan: DailyTrainingPlan) {
        val raw = plan.date.toString() + "\n" + plan.prompts.joinToString("\n") {
            listOf(it.id, it.focus.name, it.prompt.replace("|", " ")).joinToString("|")
        }
        secureStore.put(key, raw.toByteArray(Charsets.UTF_8))
    }

    fun load(): DailyTrainingPlan? {
        val raw = secureStore.get(key)?.toString(Charsets.UTF_8) ?: return null
        val lines = raw.lines()
        val date = lines.firstOrNull()?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val prompts = lines.drop(1).mapNotNull { line ->
            val p = line.split("|", limit = 3)
            if (p.size != 3) null else TrainingPrompt(p[0], runCatching { TrainingFocus.valueOf(p[1]) }.getOrDefault(TrainingFocus.CURIOSITY), p[2])
        }
        return if (prompts.size == 25) DailyTrainingPlan(date, prompts) else null
    }

    fun clear() = secureStore.delete(key)
}
