package com.williamfq.domain.models

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LocationExtensions {


    fun Location.toDomainLocation(): com.williamfq.domain.models.Location {
        return com.williamfq.domain.models.Location(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            speed = speed,
            bearing = bearing,
            time = time
        )
    }
    suspend fun Location.toDomainLocationWithGeocoding(geocoder: Geocoder): com.williamfq.domain.models.Location {
        val address = getAddressFromLocation(geocoder, latitude, longitude)

        return com.williamfq.domain.models.Location(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            speed = speed,
            bearing = bearing,
            time = time,
            country = address?.countryName,
            city = address?.locality,
            state = address?.adminArea,
            zipCode = address?.postalCode,
            address = address?.getAddressLine(0) ?: "",
            countryCode = address?.countryCode
        )
    }
    private suspend fun getAddressFromLocation(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): Address? = suspendCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: List<Address>) {
                    continuation.resume(addresses.firstOrNull())
                }

                override fun onError(errorMessage: String?) {
                    continuation.resume(null)
                }
            })
        } else {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                continuation.resume(addresses?.firstOrNull())
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
    private fun Location.distanceTo(other: com.williamfq.domain.models.Location): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            latitude,
            longitude,
            other.latitude,
            other.longitude,
            results
        )
        return results[0]
    }
    fun Location.isWithinRadius(
        center: com.williamfq.domain.models.Location,
        radiusInMeters: Float
    ): Boolean {
        return distanceTo(center) <= radiusInMeters
    }
}