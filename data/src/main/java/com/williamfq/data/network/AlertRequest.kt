// data/src/main/java/com/williamfq/data/network/models/AlertRequest.kt
package com.williamfq.data.network.models

data class AlertRequest(
    val message: String,
    val userId: String,
    val location: LocationRequest?
)

data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)
