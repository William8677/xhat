package com.williamfq.data.mapper

import com.williamfq.domain.models.PanicAlert
import com.williamfq.data.entities.PanicAlertEntity
import com.williamfq.domain.models.PanicAlertStatus
import com.williamfq.data.entities.PanicAlertStatusEntity
import com.williamfq.domain.models.Location
import com.williamfq.data.entities.LocationEntity

fun PanicAlert.toEntity(): PanicAlertEntity =
    PanicAlertEntity(
        id = this.id,
        message = this.message,
        userId = this.userId,
        location = this.location?.toLocationEntity(this.userId),
        timestamp = this.timestamp,
        status = this.status.toEntity()
    )

fun PanicAlertEntity.toDomain(): PanicAlert =
    PanicAlert(
        id = this.id,
        message = this.message,
        userId = this.userId,
        location = this.location?.toDomain(),
        timestamp = this.timestamp,
        status = this.status.toDomain()
    )

fun PanicAlertStatus.toEntity(): PanicAlertStatusEntity =
    when (this) {
        PanicAlertStatus.SENT -> PanicAlertStatusEntity.SENT
        PanicAlertStatus.FAILED -> PanicAlertStatusEntity.FAILED
        PanicAlertStatus.SENDING -> PanicAlertStatusEntity.SENDING
        PanicAlertStatus.DELIVERED -> PanicAlertStatusEntity.DELIVERED
        PanicAlertStatus.READ -> PanicAlertStatusEntity.READ
        PanicAlertStatus.CANCELLED -> PanicAlertStatusEntity.CANCELLED
        PanicAlertStatus.DELETED -> PanicAlertStatusEntity.DELETED
        PanicAlertStatus.ERROR -> PanicAlertStatusEntity.ERROR
    }

fun PanicAlertStatusEntity.toDomain(): PanicAlertStatus =
    when (this) {
        PanicAlertStatusEntity.SENT -> PanicAlertStatus.SENT
        PanicAlertStatusEntity.FAILED -> PanicAlertStatus.FAILED
        PanicAlertStatusEntity.SENDING -> PanicAlertStatus.SENDING
        PanicAlertStatusEntity.DELIVERED -> PanicAlertStatus.DELIVERED
        PanicAlertStatusEntity.READ -> PanicAlertStatus.READ
        PanicAlertStatusEntity.ERROR -> PanicAlertStatus.ERROR
        PanicAlertStatusEntity.CANCELLED -> PanicAlertStatus.CANCELLED
        PanicAlertStatusEntity.DELETED -> PanicAlertStatus.DELETED
    }

fun Location.toLocationEntity(userId: String): LocationEntity =
    LocationEntity(
        userId = userId,
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        altitude = this.altitude,
        speed = this.speed,
        bearing = this.bearing,
        timestamp = this.time
    )

fun LocationEntity.toDomain(): Location =
    Location(
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        altitude = this.altitude,
        speed = this.speed,
        bearing = this.bearing,
        time = this.timestamp
    )