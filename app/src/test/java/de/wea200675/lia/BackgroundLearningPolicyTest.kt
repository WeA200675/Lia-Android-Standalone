package de.wea200675.lia

import de.wea200675.lia.core.BackgroundLearningAction
import de.wea200675.lia.core.BackgroundLearningPolicy
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BackgroundLearningPolicyTest {
    private val today = LocalDate.of(2026, 9, 21)

    @Test fun sameDayPlanIsKeptAcrossHourlyRuns() {
        assertEquals(BackgroundLearningAction.KEEP_CURRENT, BackgroundLearningPolicy.decide(today, today).action)
    }

    @Test fun newDayGeneratesExactlyOneReplacementPlan() {
        assertEquals(BackgroundLearningAction.GENERATE_TODAY, BackgroundLearningPolicy.decide(today.minusDays(1), today).action)
        assertEquals(BackgroundLearningAction.GENERATE_TODAY, BackgroundLearningPolicy.decide(null, today).action)
    }

    @Test fun promptCountIsBoundedToTwentyFive() {
        assertEquals(25, BackgroundLearningPolicy.boundedPromptCount(100))
        assertEquals(0, BackgroundLearningPolicy.boundedPromptCount(-4))
    }

    @Test fun transientFailuresRequestRetry() {
        assertEquals(BackgroundLearningAction.RETRY, BackgroundLearningPolicy.decide(today, today, consecutiveFailures = 1).action)
    }
}
