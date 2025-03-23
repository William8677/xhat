/*
 * Updated: 2025-02-06 20:59:11
 * Author: William8677
 */
package com.williamfq.data.mapper

import com.williamfq.domain.model.*
import com.williamfq.data.entities.*

fun ChatMessage.toEntity(): MessageEntity =
    MessageEntity(
        id = this.id.toLong(),
        messageId = this.messageId,
        chatId = this.chatId,
        senderId = this.senderId,
        recipientId = this.recipientId,
        messageContent = this.content,
        timestamp = this.timestamp,
        isRead = this.isRead,
        isSent = this.isSent,
        isDeleted = this.isDeleted,
        messageType = MessageTypeEntity.valueOf(this.type.name),
        messageStatus = MessageStatusEntity.valueOf(this.status.name),
        messageAttachments = this.attachments.map { it.toEntity() },
        messageMentions = this.mentions,
        messageReplyTo = this.replyTo,
        messageAutoDestructAt = this.autoDestructAt,
        messageIsEdited = this.isEdited,
        messageEditedAt = this.editedAt,
        messageDeletionType = DeletionTypeEntity.valueOf(this.deletionType.name),
        messageExtraData = this.extraData,
        messageRoomId = this.roomId,
        messageUsername = this.username,
        messageIsMediaMessage = this.isMediaMessage,
        messageCanBeEdited = this.canBeEdited,
        messageAttachmentUrls = this.getAttachmentUrls()
    )

fun MessageEntity.toDomain(): ChatMessage =
    ChatMessage(
        id = this.id.toInt(),
        messageId = this.messageId,
        chatId = this.chatId,
        senderId = this.senderId,
        recipientId = this.recipientId,
        content = this.messageContent,
        timestamp = this.timestamp,
        isRead = this.isRead,
        isSent = this.isSent,
        isDeleted = this.isDeleted,
        type = MessageType.valueOf(this.messageType.name),
        status = MessageStatus.valueOf(this.messageStatus.name),
        attachments = this.messageAttachments.map { it.toDomain() },
        mentions = this.messageMentions,
        replyTo = this.messageReplyTo,
        autoDestructAt = this.messageAutoDestructAt,
        isEdited = this.messageIsEdited,
        editedAt = this.messageEditedAt,
        deletionType = DeletionType.valueOf(this.messageDeletionType.name),
        extraData = this.messageExtraData,
        roomId = this.messageRoomId ?: "",
        username = this.messageUsername ?: ""
    )