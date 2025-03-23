package com.williamfq.data.repository

import com.williamfq.data.services.AlertService
import com.williamfq.data.network.AlertApiService
import com.williamfq.data.network.models.AlertRequest
import com.williamfq.data.network.models.LocationRequest
import com.williamfq.domain.models.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertServiceImpl @Inject constructor(
    private val alertApiService: AlertApiService
) : AlertService {

    override suspend fun sendAlert(message: String, userId: String, location: Location?) {
        val locationRequest = location?.let {
            LocationRequest(latitude = it.latitude, longitude = it.longitude)
        }
        val request = AlertRequest(
            message = message,
            userId = userId,
            location = locationRequest
        )
        alertApiService.sendPanicAlert(request)
    }
}
