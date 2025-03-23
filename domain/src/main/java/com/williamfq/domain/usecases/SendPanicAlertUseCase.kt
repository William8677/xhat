package com.williamfq.domain.usecases

import com.williamfq.domain.models.Location
import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.models.PanicAlertStatus
import com.williamfq.domain.models.AlertPriority
import com.williamfq.domain.repository.PanicRepository
import com.williamfq.domain.repository.ContactRepository
import com.williamfq.domain.service.MessagingService
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SendPanicAlertUseCase @Inject constructor(
    private val panicRepository: PanicRepository,
    private val contactRepository: ContactRepository,
    private val messagingService: MessagingService
) {
    suspend operator fun invoke(
        message: String,
        userId: String,
        location: Location,
        priority: AlertPriority = AlertPriority.HIGH
    ) = coroutineScope {
        // Obtener contactos según prioridad
        val contacts = when (priority) {
            AlertPriority.HIGH -> contactRepository.getMostFrequentContacts(5)
            AlertPriority.MEDIUM -> contactRepository.getMostFrequentContacts(3)
            AlertPriority.LOW -> contactRepository.getMostFrequentContacts(1)
        }

        if (contacts.isEmpty()) {
            throw NoEmergencyContactsException("No hay contactos configurados para alertas")
        }

        // Crear la alerta de pánico
        val alert = PanicAlert(
            message = message,
            userId = userId,
            location = location,
            timestamp = System.currentTimeMillis(),
            status = PanicAlertStatus.SENDING,
            priority = priority
        )

        // Guardar la alerta
        val alertId = panicRepository.insertPanicAlert(alert)

        var successCount = 0
        // Enviar mensaje a cada contacto
        contacts.forEach { contact ->
            try {
                messagingService.sendEmergencyMessage(
                    recipientId = contact.id,
                    alert = alert.copy(id = alertId)
                )
                successCount++
            } catch (e: Exception) {
                // Continuar con los siguientes contactos
                panicRepository.updatePanicAlertStatus(alertId, PanicAlertStatus.ERROR)
            }
        }

        // Actualizar estado final
        val finalStatus = when {
            successCount == contacts.size -> PanicAlertStatus.SENT
            successCount > 0 -> PanicAlertStatus.SENT // Algunos mensajes enviados
            else -> PanicAlertStatus.ERROR // Ningún mensaje enviado
        }

        panicRepository.updatePanicAlertStatus(alertId, finalStatus)

        // Retornar el ID de la alerta
        alertId
    }
}

class NoEmergencyContactsException(message: String) : Exception(message)