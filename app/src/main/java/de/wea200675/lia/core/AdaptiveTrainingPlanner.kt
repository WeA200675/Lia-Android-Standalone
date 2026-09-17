package de.wea200675.lia.core

import java.time.LocalDate

data class LearningSignals(
    val interests: Set<String> = emptySet(),
    val conversationThemes: Set<String> = emptySet(),
    val anonymizedWebTopics: Set<String> = emptySet()
)

object AdaptiveTrainingPlanner {
    fun forDate(date: LocalDate, signals: LearningSignals): DailyTrainingPlan {
        val topics = (signals.interests + signals.conversationThemes + signals.anonymizedWebTopics)
            .map { it.trim() }
            .filter { it.length in 2..80 }
            .distinct()
            .take(10)
        val base = DailyTrainingPlanner.forDate(date).prompts
        val adaptive = topics.mapIndexed { index, topic ->
            TrainingPrompt(
                id = date.toString() + "-adaptive-" + index,
                focus = TrainingFocus.CURIOSITY,
                prompt = "Möchtest du heute etwas über " + topic + " erzählen oder erfahren?"
            )
        }
        return DailyTrainingPlan(date, (adaptive + base).distinctBy { it.prompt }.take(25).let {
            it + base.drop(it.size).take(25 - it.size)
        })
    }
}
