
package com.williamfq.domain.location

import com.williamfq.domain.models.Location
import kotlinx.coroutines.flow.Flow

interface LocationTracker {

    fun isLocationPermissionGranted(): Boolean
    fun getCurrentLocation(): Flow<Location?>
    fun startLocationUpdates(): Flow<Location>
    fun getLocationUpdates(): Flow<Location>
    fun stopLocationUpdates()
}