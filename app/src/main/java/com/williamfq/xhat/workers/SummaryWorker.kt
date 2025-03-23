package com.williamfq.xhat.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.williamfq.data.repository.SummaryRepository
import com.williamfq.domain.usecases.GenerateSummaryUseCase
import com.williamfq.data.models.ActivityDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Worker para generar resúmenes de actividad de usuario.
 * Actualizado por William8677 el 2025-01-05 05:56:30
 */
@HiltWorker
class SummaryWorker @Inject constructor(
    @ApplicationContext context: Context,
    workerParams: WorkerParameters,
    private val summaryRepository: SummaryRepository,
    private val generateSummaryUseCase: GenerateSummaryUseCase,
    private val json: Json
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: run {
            Timber.e("UserId no proporcionado")
            return Result.failure()
        }
        val period = inputData.getString("period") ?: "weekly"

        return try {
            // Recuperar datos del repositorio
            val activityDataJson = summaryRepository.getUserActivitySummary(userId)

            // Verificar el tipo de activityDataJson
            Timber.d("Procesando datos de actividad para usuario: $userId")

            // Convertir JSON usando Kotlinx Serialization
            val activityData = try {
                json.decodeFromString<ActivityDataDto>(activityDataJson.toString()).toDomain()
            } catch (e: Exception) {
                Timber.e(e, "Error parseando JSON: $activityDataJson")
                return Result.failure()
            }

            // Generar el resumen
            generateSummaryUseCase(activityData, period)

            Timber.i("Resumen generado exitosamente para userId=$userId")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error generando el resumen para userId=$userId, period=$period: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SummaryWorker"
        private const val CURRENT_USER = "William8677"
    }
}