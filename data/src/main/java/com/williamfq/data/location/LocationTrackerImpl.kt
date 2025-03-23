package com.williamfq.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.williamfq.domain.location.LocationTracker
import com.williamfq.domain.models.Location
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import android.location.Location as AndroidLocation

@Singleton
class LocationTrackerImpl @Inject constructor(
    private val context: Context
) : LocationTracker {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationUpdates = MutableLiveData<AndroidLocation>()
    val locationUpdates: LiveData<AndroidLocation> get() = _locationUpdates

    override fun isLocationPermissionGranted(): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Flow<Location?> = callbackFlow {
        if (!isLocationPermissionGranted()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            trySend(location?.toDomainLocation()).isSuccess
        }.addOnFailureListener { exception ->
            Timber.tag(TAG).e("Error fetching location: ${exception.message}")
            trySend(null).isSuccess
        }

        awaitClose { cancellationTokenSource.cancel() }
    }

    override fun startLocationUpdates(): Flow<Location> = getLocationUpdates()

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!isLocationPermissionGranted()) {
            close()
            return@callbackFlow
        }

        // Construir la solicitud de ubicación utilizando el nuevo Builder
        val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
            .setIntervalMillis(UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    location.toDomainLocation().let { domainLocation ->
                        trySend(domainLocation).isSuccess
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { exception ->
            Timber.tag(TAG).e("Failed to start location updates: ${exception.message}")
            close(exception)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }.catch { exception ->
        Timber.tag(TAG).e("Error during location updates: ${exception.message}")
    }.onCompletion {
        Timber.tag(TAG).d("Location updates completed")
    }

    override fun stopLocationUpdates() {
        // La cancelación de las actualizaciones se gestiona a través del Flow (awaitClose)
        // Si fuera necesario, se podría guardar una referencia al callback para removerlo aquí.
    }

    private fun AndroidLocation.toDomainLocation(): Location {
        return Location(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            speed = speed,
            time = time,
            bearing = bearing
        )
    }

    companion object {
        private const val TAG = "LocationTracker"
        private const val UPDATE_INTERVAL = 5000L // 5 segundos
        private const val FASTEST_INTERVAL = 2000L // 2 segundos
    }
}
