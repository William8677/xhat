package com.williamfq.domain.models

data class LocationUpdate(
    val userId: String,
    val chatId: String,
    val chatType: String,
    val location: Location,
    val timestamp: Long,
    val accuracy: Float,
    val speed: Float,
    val altitude: Double,
    val bearing: Float
)