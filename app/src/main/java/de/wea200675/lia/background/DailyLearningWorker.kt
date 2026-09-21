package de.wea200675.lia.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.BackoffPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.wea200675.lia.core.AndroidSecureStore
import de.wea200675.lia.core.BackgroundLearningAction
import de.wea200675.lia.core.BackgroundLearningPolicy
import de.wea200675.lia.core.DailyTrainingPlanner
import de.wea200675.lia.core.TrainingCache
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class DailyLearningWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val today = LocalDate.now()
            val cache = TrainingCache(AndroidSecureStore(applicationContext))
            val cached = cache.load()
            when (BackgroundLearningPolicy.decide(cached?.date, today).action) {
                BackgroundLearningAction.KEEP_CURRENT -> Unit
                BackgroundLearningAction.GENERATE_TODAY -> cache.save(DailyTrainingPlanner.forDate(today))
                BackgroundLearningAction.RETRY -> return Result.retry()
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
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
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
