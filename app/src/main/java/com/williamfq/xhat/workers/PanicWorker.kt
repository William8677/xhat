package com.williamfq.xhat.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PanicWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Aquí implementas la lógica del PanicWorker
            // Ejemplo: enviar una alerta de pánico o guardar datos en la base de datos
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
