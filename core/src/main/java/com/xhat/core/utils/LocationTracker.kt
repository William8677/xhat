package com.xhat.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(private val context: Context) {

    // Obtener la instancia de FusedLocationProviderClient correctamente
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationFlow = MutableStateFlow<Location?>(null)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 15000L
    )
        .setMinUpdateIntervalMillis(10000L)
        .build()


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                locationFlow.value = it
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getLocationUpdates(): StateFlow<Location?> = locationFlow
}
