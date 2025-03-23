/*
 * Updated: 2025-01-24 00:48:28
 * Author: William8677
 */

package com.williamfq.domain.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.williamfq.domain.models.PanicAlert

interface MessagingService {
    suspend fun sendEmergencyMessage(recipientId: String, alert: PanicAlert) {
        try {
            val message = createEmergencyMessage(alert, recipientId)
            FirebaseMessaging.getInstance().send(message)
        } catch (e: Exception) {
            throw MessagingException("Failed to send emergency message", e)
        }
    }

    private fun createEmergencyMessage(alert: PanicAlert, recipientId: String): RemoteMessage {
        return RemoteMessage.Builder("$MESSAGING_TOPIC/$recipientId")
            .addData("type", "EMERGENCY_ALERT")
            .addData("alertId", alert.id.toString())
            .addData("message", alert.message)
            .addData("userId", alert.userId)
            .addData("latitude", alert.location?.latitude?.toString() ?: "")
            .addData("longitude", alert.location?.longitude?.toString() ?: "")
            .addData("timestamp", alert.timestamp.toString())
            .addData("notificationTitle", "Emergency Alert")
            .addData("notificationBody", alert.message)
            .addData("priority", "high")
            .setTtl(EMERGENCY_MESSAGE_TTL)
            .build()
    }

    companion object {
        private const val MESSAGING_TOPIC = "/topics/emergency"
        private const val EMERGENCY_MESSAGE_TTL = 14400 // 4 horas en segundos (60 * 60 * 4)
    }
}

class MessagingException(message: String, cause: Throwable? = null) : Exception(message, cause)