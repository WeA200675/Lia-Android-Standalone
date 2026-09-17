package de.wea200675.lia

import de.wea200675.lia.core.DailyTrainingPlanner
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DailyTrainingPlanTest {
    @Test fun planHasTwentyFivePromptsAndStableIds() {
        val plan = DailyTrainingPlanner.forDate(LocalDate.of(2026, 9, 17))
        assertEquals(25, plan.prompts.size)
        assertEquals(25, plan.prompts.map { it.id }.toSet().size)
    }

    @Test fun adjacentDaysVaryPromptOrder() {
        val first = DailyTrainingPlanner.forDate(LocalDate.of(2026, 9, 17))
        val next = DailyTrainingPlanner.forDate(LocalDate.of(2026, 9, 18))
        assertNotEquals(first.prompts.map { it.prompt }, next.prompts.map { it.prompt })
    }
}
