package de.wea200675.lia.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import de.wea200675.lia.core.DailyTrainingPlanner
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class DailyLearningWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // Preparation is intentionally side-effect free here; UI presents prompts later.
        DailyTrainingPlanner.forDate(LocalDate.now())
        return Result.success()
    }

    companion object {
        private const val NAME = "lia-daily-learning"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<DailyLearningWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
