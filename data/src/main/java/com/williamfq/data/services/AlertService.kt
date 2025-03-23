package com.williamfq.data.services

import com.williamfq.domain.models.Location

interface AlertService {
    /**
     * Envía una alerta.
     *
     * @param message El mensaje de la alerta.
     * @param userId El ID del usuario que envía la alerta.
     * @param location La ubicación asociada con la alerta (opcional).
     */
    suspend fun sendAlert(message: String, userId: String, location: Location? = null)
}
