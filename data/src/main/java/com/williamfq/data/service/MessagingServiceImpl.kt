package com.williamfq.data.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.service.MessagingException
import com.williamfq.domain.service.MessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MessagingServiceImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) : MessagingService {

    override suspend fun sendEmergencyMessage(recipientId: String, alert: PanicAlert) {
        try {
            val message = createEmergencyMessage(alert, recipientId)
            withContext(Dispatchers.IO) {
                firebaseMessaging.send(message) // Mantenido por compatibilidad actual, pero considera sendAsync en el futuro
            }
        } catch (e: Exception) {
            throw MessagingException("No se pudo enviar el mensaje de emergencia", e)
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
            .addData("notificationTitle", "¡Alerta de Emergencia!")
            .addData("notificationBody", alert.message)
            .addData("priority", "high")
            .setTtl(EMERGENCY_MESSAGE_TTL)
            .build()
    }

    companion object {
        private const val MESSAGING_TOPIC = "/topics/emergency"
        private const val EMERGENCY_MESSAGE_TTL = 14400 // 4 horas
    }
}