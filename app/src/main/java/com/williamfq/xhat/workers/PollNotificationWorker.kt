package com.williamfq.xhat.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PollNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val pollId = inputData.getInt("pollId", -1)
        // Implementa lógica de notificación aquí
        return Result.success()
    }
}
