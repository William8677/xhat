package com.williamfq.domain.repository

import com.williamfq.domain.models.Location
import com.williamfq.domain.models.LocationUpdate

interface LocationRepository {
    suspend fun updateLocation(locationUpdate: LocationUpdate)
    suspend fun saveLocationHistory(locationUpdate: LocationUpdate)
    suspend fun stopLocationSharing(userId: String)
    suspend fun notifyLocationUpdate(contactId: String, locationUpdate: LocationUpdate)
    suspend fun getLastKnownLocation(): Location?
    suspend fun getLocationHistory(userId: String): List<LocationUpdate>
    suspend fun clearLocationHistory(userId: String)
    suspend fun getSharingContacts(): List<String>
}