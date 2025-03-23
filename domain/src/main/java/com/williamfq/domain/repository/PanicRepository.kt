package com.williamfq.domain.repository

import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.models.PanicAlertStatus

interface PanicRepository {
    suspend fun insertPanicAlert(alert: PanicAlert): Long
    suspend fun getPanicAlertsByUserId(userId: String): List<PanicAlert>
    suspend fun updatePanicAlertStatus(alertId: Long, status: PanicAlertStatus)
    suspend fun getPanicAlert(alertId: Long): PanicAlert?
    suspend fun deletePanicAlert(alertId: Long)
}