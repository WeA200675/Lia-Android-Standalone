package de.wea200675.lia.core

import java.time.LocalDate

/** Single source for the current 25-prompt plan used by UI and background work. */
class DailyPlanRepository(
    private val cache: TrainingCache,
    private val planner: (LocalDate) -> DailyTrainingPlan = DailyTrainingPlanner::forDate
) {
    fun forDate(date: LocalDate): DailyTrainingPlan {
        val cached = runCatching { cache.load() }.getOrNull()
        if (cached?.date == date) return cached

        val plan = planner(date)
        // The plan remains usable when Keystore persistence is temporarily unavailable.
        runCatching { cache.save(plan) }
        return plan
    }
}
