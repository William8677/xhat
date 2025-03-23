// LocationSharingManager.kt: Clase para compartir ubicación en tiempo real
package com.williamfq.xhat.manager

import android.Manifest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class LocationSharingManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun shareLocation(chatId: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val locationData = hashMapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                // Lógica para compartir la ubicación en el chat especificado
                println("Ubicación compartida: Latitud ${location.latitude}, Longitud ${location.longitude}")
            }
        }.addOnFailureListener {
            println("Error al obtener la ubicación: ${it.message}")
        }
    }
}
