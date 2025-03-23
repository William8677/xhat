/*
 * Updated: 2025-02-07 02:09:47
 * Author: William8677
 */
package com.williamfq.data.mapper

import com.williamfq.data.entities.MessageStatusEntity
import com.williamfq.domain.model.MessageStatus

/**
 * Mapea el estado del mensaje del modelo de dominio a la entidad
 */
fun MessageStatus.toEntity(): MessageStatusEntity = when (this) {
    MessageStatus.SENDING -> MessageStatusEntity.SENDING
    MessageStatus.SENT -> MessageStatusEntity.SENT
    MessageStatus.FAILED -> MessageStatusEntity.FAILED
    MessageStatus.DELIVERED -> MessageStatusEntity.DELIVERED
    MessageStatus.READ -> MessageStatusEntity.READ
    MessageStatus.ERROR -> MessageStatusEntity.ERROR
    MessageStatus.DELETED -> MessageStatusEntity.DELETED
    MessageStatus.EDITED -> MessageStatusEntity.EDITED
    MessageStatus.AUTO_DESTRUCTED -> MessageStatusEntity.AUTO_DESTRUCTED
    MessageStatus.UNKNOWN -> MessageStatusEntity.UNKNOWN
    MessageStatus.BLOCKED -> MessageStatusEntity.BLOCKED
}

/**
 * Mapea el estado del mensaje de la entidad al modelo de dominio
 */
fun MessageStatusEntity.toDomain(): MessageStatus = when (this) {
    MessageStatusEntity.SENDING -> MessageStatus.SENDING
    MessageStatusEntity.SENT -> MessageStatus.SENT
    MessageStatusEntity.FAILED -> MessageStatus.FAILED
    MessageStatusEntity.DELIVERED -> MessageStatus.DELIVERED
    MessageStatusEntity.READ -> MessageStatus.READ
    MessageStatusEntity.ERROR -> MessageStatus.ERROR
    MessageStatusEntity.DELETED -> MessageStatus.DELETED
    MessageStatusEntity.EDITED -> MessageStatus.EDITED
    MessageStatusEntity.AUTO_DESTRUCTED -> MessageStatus.AUTO_DESTRUCTED
    MessageStatusEntity.UNKNOWN -> MessageStatus.UNKNOWN
    MessageStatusEntity.BLOCKED -> MessageStatus.BLOCKED
}