/*
 * Updated: 2025-02-07 01:10:01
 * Author: William8677
 */
package com.williamfq.data.mapper

import com.williamfq.data.entities.MessageEntity
import com.williamfq.data.entities.MessageTypeEntity
import com.williamfq.data.entities.MessageStatusEntity
import com.williamfq.data.entities.DeletionTypeEntity
import com.williamfq.data.entities.MessageAttachmentEntity
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.model.MessageType
import com.williamfq.domain.model.MessageStatus
import com.williamfq.domain.model.DeletionType
import com.williamfq.domain.model.MessageAttachment
import javax.inject.Inject

/**
 * Mapper para convertir entre MessageEntity y ChatMessage
 */
class MessageMapper @Inject constructor() {

    fun mapToDomain(entity: MessageEntity): ChatMessage {
        return ChatMessage(
            id = entity.id.toInt(),
            messageId = entity.messageId,
            chatId = entity.chatId,
            senderId = entity.senderId,
            recipientId = entity.recipientId,
            content = entity.messageContent,
            isRead = entity.isRead,
            isSent = entity.isSent,
            isDeleted = entity.isDeleted,
            status = MessageStatus.valueOf(entity.messageStatus.name),
            roomId = entity.messageRoomId ?: "",
            username = entity.messageUsername ?: "",
            type = MessageType.valueOf(entity.messageType.name),
            timestamp = entity.timestamp,
            replyTo = entity.messageReplyTo,
            mentions = entity.messageMentions,
            attachments = entity.messageAttachments.map { AttachmentMapper.mapToDomain(it) },
            extraData = entity.messageExtraData,
            isEdited = entity.messageIsEdited,
            editedAt = entity.messageEditedAt,
            deletionType = DeletionType.valueOf(entity.messageDeletionType.name),
            autoDestructAt = entity.messageAutoDestructAt
        )
    }

    fun mapToEntity(domain: ChatMessage): MessageEntity {
        return MessageEntity(
            id = domain.id.toLong(),
            messageId = domain.messageId,
            chatId = domain.chatId,
            senderId = domain.senderId,
            recipientId = domain.recipientId,
            messageContent = domain.content,
            timestamp = domain.timestamp,
            isRead = domain.isRead,
            isSent = domain.isSent,
            isDeleted = domain.isDeleted,
            messageType = MessageTypeEntity.valueOf(domain.type.name),
            messageStatus = MessageStatusEntity.valueOf(domain.status.name),
            messageAttachments = domain.attachments.map { AttachmentMapper.mapToEntity(it) },
            messageExtraData = domain.extraData,
            messageMentions = domain.mentions,
            messageReplyTo = domain.replyTo,
            messageRoomId = domain.roomId,
            messageUsername = domain.username,
            messageAutoDestructAt = domain.autoDestructAt,
            messageIsEdited = domain.isEdited,
            messageEditedAt = domain.editedAt,
            messageDeletionType = DeletionTypeEntity.valueOf(domain.deletionType.name),
            messageIsMediaMessage = domain.isMediaMessage,
            messageCanBeEdited = domain.canBeEdited,
            messageAttachmentUrls = domain.getAttachmentUrls()
        )
    }
}

/**
 * Objeto para manejar el mapeo de adjuntos
 */
object AttachmentMapper {
    fun mapToDomain(entity: MessageAttachmentEntity): MessageAttachment {
        // Implementar el mapeo de MessageAttachmentEntity a MessageAttachment
        TODO("Implement attachment mapping")
    }

    fun mapToEntity(domain: MessageAttachment): MessageAttachmentEntity {
        // Implementar el mapeo de MessageAttachment a MessageAttachmentEntity
        TODO("Implement attachment mapping")
    }
}