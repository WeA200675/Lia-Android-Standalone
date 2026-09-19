package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyPlanEvolutionTest {
    @Test fun developmentStageInfluencesPromptRotation() {
        val date = LocalDate.of(2026, 1, 10)
        val foundation = DailyTrainingPlanner.forDate(date, DevelopmentStage.FOUNDATION)
        val dialogue = DailyTrainingPlanner.forDate(date, DevelopmentStage.DIALOGUE)
        assertNotEquals(foundation.prompts.first().id, dialogue.prompts.first().id)
        assertTrue(dialogue.prompts.all { it.id.contains("DIALOGUE") })
    }

    @Test fun everyStageStillProducesExactlyTwentyFiveOptionalPrompts() {
        DevelopmentStage.values().forEach { stage ->
            val plan = DailyTrainingPlanner.forDate(LocalDate.of(2026, 2, 1), stage)
            assertTrue(plan.prompts.size == 25)
            assertTrue(plan.prompts.all { it.optional })
        }
    }
}
