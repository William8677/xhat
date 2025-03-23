// PanicAlertRepository.kt
package com.williamfq.domain.repository

interface PanicAlertRepository {
    suspend fun sendAlert(message: String)
}
