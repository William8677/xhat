package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panic_alerts")
data class PanicAlertEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "alert_id")
    val id: Long = 0,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @Embedded(prefix = "location_")
    val location: LocationEntity?,

    @ColumnInfo(name = "alert_timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "alert_status")
    val status: PanicAlertStatusEntity = PanicAlertStatusEntity.SENDING
)