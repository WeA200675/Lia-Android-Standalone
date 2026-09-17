package de.wea200675.lia.core

import java.time.LocalDate

enum class TrainingFocus { MEMORY, LANGUAGE, EMOTIONS, SOCIAL, DAILY_LIFE, CURIOSITY }

data class TrainingPrompt(
    val id: String,
    val focus: TrainingFocus,
    val prompt: String,
    val optional: Boolean = true
)

data class DailyTrainingPlan(
    val date: LocalDate,
    val prompts: List<TrainingPrompt>
) {
    init { require(prompts.size == 25) { "A daily plan must contain exactly 25 prompts" } }
}

object DailyTrainingPlanner {
    private val seeds = listOf(
        TrainingFocus.MEMORY to "Was war heute ein schöner kleiner Moment?",
        TrainingFocus.LANGUAGE to "Welches Wort möchtest du heute genauer kennenlernen?",
        TrainingFocus.EMOTIONS to "Wie fühlt sich dein Tag gerade an?",
        TrainingFocus.SOCIAL to "Wem möchtest du heute eine Freude machen?",
        TrainingFocus.DAILY_LIFE to "Was wäre heute eine angenehme kleine Hilfe?",
        TrainingFocus.CURIOSITY to "Was möchtest du heute entdecken oder verstehen?"
    )

    fun forDate(date: LocalDate): DailyTrainingPlan {
        val prompts = (0 until 25).map { index ->
            val (focus, text) = seeds[(index + date.dayOfYear) % seeds.size]
            TrainingPrompt(date.toString() + "-" + index, focus, text)
        }
        return DailyTrainingPlan(date, prompts)
    }
}
