package de.wea200675.lia

import de.wea200675.lia.core.*
import org.junit.Assert.*
import org.junit.Test

class LearningEvolutionTest {
    @Test fun advancesDevelopmentStagesFromAcceptedSignals() {
        val start = LearningEvolutionState(DevelopmentStage.FOUNDATION, 39, 0, emptySet())
        val next = LearningEvolution.accept(start, listOf(LearningSignal(LearningSignalKind.FEEDBACK, "Das war hilfreich.")))
        assertEquals(DevelopmentStage.EMPATHY, next.stage)
        assertEquals(setOf("feedback"), next.focusAreas)
    }

    @Test fun boundsDailySignalsAndRejectsOversizedInput() {
        val signals = (1..40).map { LearningSignal(LearningSignalKind.INTEREST, "x".repeat(if (it == 40) 501 else 5)) }
        val next = LearningEvolution.accept(LearningEvolutionState(DevelopmentStage.FOUNDATION, 0, 0, emptySet()), signals)
        assertEquals(LearningEvolution.MAX_SIGNALS_PER_DAY, next.acceptedSignals)
        assertEquals(0, next.rejectedSignals)
    }

    @Test fun neverRegressesDevelopmentStage() {
        val state = LearningEvolutionState(DevelopmentStage.DIALOGUE, 100, 0, emptySet())
        val next = LearningEvolution.accept(state, listOf(LearningSignal(LearningSignalKind.DAILY_IMPULSE, "Gut.")))
        assertEquals(DevelopmentStage.DIALOGUE, next.stage)
    }
}
