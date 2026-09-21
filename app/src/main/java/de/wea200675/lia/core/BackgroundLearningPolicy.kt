package de.wea200675.lia.core

import java.time.LocalDate

enum class BackgroundLearningAction { KEEP_CURRENT, GENERATE_TODAY, RETRY }

data class BackgroundLearningDecision(val action: BackgroundLearningAction, val reason: String)

/** Idempotent, bounded policy for hourly background learning. */
object BackgroundLearningPolicy {
    const val MAX_PROMPTS = 25
    const val MAX_FAILURES_BEFORE_RETRY = 3

    fun decide(cachedDate: LocalDate?, today: LocalDate, consecutiveFailures: Int = 0): BackgroundLearningDecision = when {
        consecutiveFailures > 0 -> BackgroundLearningDecision(BackgroundLearningAction.RETRY, "transient failure")
        cachedDate == today -> BackgroundLearningDecision(BackgroundLearningAction.KEEP_CURRENT, "today already prepared")
        else -> BackgroundLearningDecision(BackgroundLearningAction.GENERATE_TODAY, "new calendar day")
    }

    fun boundedPromptCount(size: Int): Int = size.coerceIn(0, MAX_PROMPTS)
}
