package com.williamfq.domain.usecases

import com.williamfq.domain.models.Location
import com.williamfq.domain.models.LocationUpdate
import com.williamfq.domain.repository.LocationRepository
import com.williamfq.domain.repository.UserRepository
import javax.inject.Inject

class UpdateLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        location: Location,
        chatId: String,
        chatType: Any
    ) {
        val locationUpdate = LocationUpdate(
            userId = userRepository.getCurrentUserId(),
            chatId = chatId,
            chatType = chatType.toString(),
            location = location,
            timestamp = System.currentTimeMillis(),
            accuracy = location.accuracy,
            speed = location.speed,
            altitude = location.altitude,
            bearing = location.bearing
        )
        locationRepository.updateLocation(locationUpdate)
    }
}