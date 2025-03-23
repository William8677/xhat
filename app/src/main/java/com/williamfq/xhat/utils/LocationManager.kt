/*
 * Updated: 2025-01-22 01:22:34
 * Author: William8677
 */

package com.williamfq.xhat.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    suspend fun getCurrentLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location = getLastLocation()
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            addresses?.firstOrNull()?.let { address ->
                LocationData(
                    country = address.countryName ?: "",
                    state = address.adminArea ?: "",
                    city = address.locality ?: "",
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getLastLocation(): Location = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        continuation.resumeWithException(Exception("No se pudo obtener la ubicación"))
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }
}

data class LocationData(
    val country: String,
    val state: String,
    val city: String,
    val latitude: Double,
    val longitude: Double
)