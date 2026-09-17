package de.wea200675.lia.core

import java.time.Duration
import java.time.Instant

data class BackgroundTrainingWindow(
    val nextRun: Instant,
    val interval: Duration = Duration.ofHours(1),
    val requiresCharging: Boolean = false,
    val requiresUnmeteredNetwork: Boolean = false
)

/** Policy for quiet background preparation; UI and voice remain user initiated. */
object BackgroundTrainingScheduler {
    fun nextWindow(now: Instant = Instant.now(), batteryPercent: Int = 100): BackgroundTrainingWindow {
        val lowBattery = batteryPercent < 25
        return BackgroundTrainingWindow(
            nextRun = now.plus(if (lowBattery) Duration.ofHours(6) else Duration.ofHours(1)),
            requiresCharging = lowBattery,
            requiresUnmeteredNetwork = false
        )
    }
}
