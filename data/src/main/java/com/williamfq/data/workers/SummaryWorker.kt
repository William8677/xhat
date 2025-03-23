package com.williamfq.app.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.williamfq.data.repository.SummaryRepository
import com.williamfq.domain.models.ActivityData
import com.williamfq.domain.usecases.GenerateSummaryUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class SummaryWorker @Inject constructor(
    @ApplicationContext context: Context,
    workerParams: WorkerParameters,
    private val summaryRepository: SummaryRepository,
    private val generateSummaryUseCase: GenerateSummaryUseCase,
    private val json: Json
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val period = inputData.getString("period") ?: "weekly"

        return try {
            val activityDataJson = summaryRepository.getUserActivitySummary(userId)
            val activityData = json.decodeFromString<ActivityData>(activityDataJson.toString())

            // Asegúrate de que los datos cumplen con lo esperado por GenerateSummaryUseCase
            generateSummaryUseCase(activityData, period)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error generando el resumen para userId=$userId")
            Result.failure()
        }
    }
}