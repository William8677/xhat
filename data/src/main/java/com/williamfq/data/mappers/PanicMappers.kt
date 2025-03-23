package com.williamfq.data.mappers

import com.williamfq.data.entities.PanicAlertEntity
import com.williamfq.data.entities.PanicAlertStatusEntity
import com.williamfq.data.entities.LocationEntity
import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.models.PanicAlertStatus
import com.williamfq.domain.models.Location

fun PanicAlert.toPanicAlertEntity() = PanicAlertEntity(
    id = id,
    message = message,
    userId = userId,
    location = location?.toLocationEntity(userId),
    timestamp = timestamp,
    status = status.toPanicAlertStatusEntity()
)

fun PanicAlertEntity.toPanicAlert() = PanicAlert(
    id = id,
    message = message,
    userId = userId,
    location = location?.toDomain(),
    timestamp = timestamp,
    status = status.toPanicAlertStatus()
)

fun PanicAlertStatus.toPanicAlertStatusEntity() = when (this) {
    PanicAlertStatus.SENDING -> PanicAlertStatusEntity.SENDING
    PanicAlertStatus.SENT -> PanicAlertStatusEntity.SENT
    PanicAlertStatus.DELIVERED -> PanicAlertStatusEntity.DELIVERED
    PanicAlertStatus.READ -> PanicAlertStatusEntity.READ
    PanicAlertStatus.ERROR -> PanicAlertStatusEntity.ERROR
    PanicAlertStatus.CANCELLED -> PanicAlertStatusEntity.CANCELLED
    PanicAlertStatus.DELETED -> PanicAlertStatusEntity.DELETED
    PanicAlertStatus.FAILED -> PanicAlertStatusEntity.FAILED
}

fun PanicAlertStatusEntity.toPanicAlertStatus() = when (this) {
    PanicAlertStatusEntity.SENDING -> PanicAlertStatus.SENDING
    PanicAlertStatusEntity.SENT -> PanicAlertStatus.SENT
    PanicAlertStatusEntity.DELIVERED -> PanicAlertStatus.DELIVERED
    PanicAlertStatusEntity.READ -> PanicAlertStatus.READ
    PanicAlertStatusEntity.ERROR -> PanicAlertStatus.ERROR
    PanicAlertStatusEntity.CANCELLED -> PanicAlertStatus.CANCELLED
    PanicAlertStatusEntity.DELETED -> PanicAlertStatus.DELETED
    PanicAlertStatusEntity.FAILED -> PanicAlertStatus.FAILED
}

fun Location.toLocationEntity(userId: String): LocationEntity = LocationEntity(
    userId = userId,
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    altitude = altitude,
    speed = speed,
    bearing = bearing,
    timestamp = time
)

fun LocationEntity.toDomain(): Location = Location(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    altitude = altitude,
    speed = speed,
    bearing = bearing,
    time = timestamp
)