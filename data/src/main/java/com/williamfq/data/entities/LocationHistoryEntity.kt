package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.williamfq.domain.models.Location

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "accuracy")
    val accuracy: Float,

    @ColumnInfo(name = "altitude")
    val altitude: Double,

    @ColumnInfo(name = "speed")
    val speed: Float,

    @ColumnInfo(name = "bearing")
    val bearing: Float,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
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
        fun fromLocation(userId: String, location: LocationEntity) = LocationHistoryEntity(
            userId = userId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            bearing = location.bearing,
            timestamp = location.timestamp
        )
    }
}