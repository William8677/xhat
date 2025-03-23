/*
 * Updated: 2025-02-05 17:39:02
 * Author: William8677
 */
package com.williamfq.data.mapper

import com.williamfq.data.entities.MessageTypeEntity
import com.williamfq.domain.model.MessageType

fun MessageType.toEntity(): MessageTypeEntity = when (this) {
    MessageType.TEXT -> MessageTypeEntity.TEXT
    MessageType.IMAGE -> MessageTypeEntity.IMAGE
    MessageType.VIDEO -> MessageTypeEntity.VIDEO
    MessageType.AUDIO -> MessageTypeEntity.AUDIO
    MessageType.FILE -> MessageTypeEntity.FILE
    MessageType.STICKER -> MessageTypeEntity.STICKER
    MessageType.SYSTEM -> MessageTypeEntity.SYSTEM
    MessageType.VOICE_NOTE -> MessageTypeEntity.VOICE_NOTE
    MessageType.LOCATION -> MessageTypeEntity.LOCATION
    MessageType.CONTACT -> MessageTypeEntity.CONTACT
    MessageType.GIF -> MessageTypeEntity.GIF
    MessageType.POLL -> MessageTypeEntity.POLL
    MessageType.DOCUMENT -> MessageTypeEntity.DOCUMENT
}

fun MessageTypeEntity.toDomain(): MessageType = when (this) {
    MessageTypeEntity.TEXT -> MessageType.TEXT
    MessageTypeEntity.IMAGE -> MessageType.IMAGE
    MessageTypeEntity.VIDEO -> MessageType.VIDEO
    MessageTypeEntity.AUDIO -> MessageType.AUDIO
    MessageTypeEntity.FILE -> MessageType.FILE
    MessageTypeEntity.STICKER -> MessageType.STICKER
    MessageTypeEntity.SYSTEM -> MessageType.SYSTEM
    MessageTypeEntity.VOICE_NOTE -> MessageType.VOICE_NOTE
    MessageTypeEntity.LOCATION -> MessageType.LOCATION
    MessageTypeEntity.CONTACT -> MessageType.CONTACT
    MessageTypeEntity.GIF -> MessageType.GIF
    MessageTypeEntity.POLL -> MessageType.POLL
    MessageTypeEntity.DOCUMENT -> MessageType.DOCUMENT
}
