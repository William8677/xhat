
package com.xhat.core.utils

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseLocationUtils {

    private val db = FirebaseFirestore.getInstance()

    fun updateRealTimeLocation(locationId: String, location: LatLng) {
        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("realTimeLocations").document(locationId).set(locationData)
    }

    fun listenToRealTimeLocation(locationId: String, onLocationUpdate: (LatLng) -> Unit) {
        db.collection("realTimeLocations").document(locationId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val latitude = snapshot.getDouble("latitude") ?: 0.0
                    val longitude = snapshot.getDouble("longitude") ?: 0.0
                    onLocationUpdate(LatLng(latitude, longitude))
                }
            }
    }
}
