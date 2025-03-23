package com.williamfq.data.services

import javax.inject.Inject
import javax.inject.Singleton
import com.williamfq.domain.models.Location

@Singleton
class AlertServiceImpl @Inject constructor(
    // Dependencias necesarias, por ejemplo, Retrofit o algún otro servicio
) : AlertService {

    override suspend fun sendAlert(message: String, userId: String, location: Location?) {
        // Implementación de envío de alerta
    }
}