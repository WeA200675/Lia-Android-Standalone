package de.wea200675.lia.core

import java.time.LocalDate

/** Single source for the current 25-prompt plan used by UI and background work. */
class DailyPlanRepository(
    private val cache: TrainingCache,
    private val evolutionStore: LearningEvolutionStore? = null,
    private val planner: (LocalDate) -> DailyTrainingPlan = DailyTrainingPlanner::forDate
) {
    fun forDate(date: LocalDate): DailyTrainingPlan {
        val cached = runCatching { cache.load() }.getOrNull()
        if (cached?.date == date) return cached

        val stage = runCatching { evolutionStore?.load()?.stage }.getOrNull()
        val plan = if (stage == null) planner(date) else DailyTrainingPlanner.forDate(date, stage)
        runCatching { cache.save(plan) }
        return plan
    }
}
