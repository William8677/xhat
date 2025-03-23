package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.williamfq.domain.models.Location
import com.williamfq.domain.models.LocationUpdate

/**
 * Representa una ubicación geográfica en la base de datos.
 * @author William8677
 * @since 2025-02-10 19:09:47
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "accuracy")
    val accuracy: Float = 0f,

    @ColumnInfo(name = "altitude")
    val altitude: Double = 0.0,

    @ColumnInfo(name = "speed")
    val speed: Float = 0f,

    @ColumnInfo(name = "bearing")
    val bearing: Float = 0f,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel() = Location(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        altitude = altitude,
        speed = speed,
        bearing = bearing,
        time = timestamp
    )

    companion object {
        fun fromLocationUpdate(locationUpdate: LocationUpdate) = LocationEntity(
            userId = locationUpdate.userId,
            latitude = locationUpdate.location.latitude,
            longitude = locationUpdate.location.longitude,
            accuracy = locationUpdate.accuracy,
            altitude = locationUpdate.altitude,
            speed = locationUpdate.speed,
            bearing = locationUpdate.bearing,
            timestamp = locationUpdate.timestamp
        )
    }
}