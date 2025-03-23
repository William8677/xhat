package com.williamfq.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Message(
    // Identificadores
    val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val replyToMessageId: String? = null,
    val replyToUserId: String? = null,
    val replyToUserName: String? = null,
    val replyToChatId: String? = null,
    val replyToMessageContent: String? = null,
    val replyToMessageTimestamp: Long? = null,
    val replyToMessageSenderName: String? = null,
    val replyToMessageSenderPhotoUrl: String? = null,
    val replyToMessageAttachments: List<String> = emptyList(),
    val replyToMessageMentions: List<String> = emptyList(),
    val replyToMessageReactions: Map<String, String> = emptyMap(),
    val replyToMessageStatus: MessageStatus = MessageStatus.SENT,
    val replyToMessageTimestampFormatted: String = LocalDateTime.now().toString(),
    val replyToMessageSenderPhoto: String? = null,
    val replyToMessageSenderPhotoThumbnail: String? = null,

    // Contenido
    val content: String,
    val text: String = content,
    val senderName: String,
    val attachments: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val senderPhotoUrl: String? = null,
    val senderPhoto: String? = null,
    val senderPhotoThumbnail: String? = null,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val isSeen: Boolean = false,
    val isPinned: Boolean = false,
    val isStarred: Boolean = false,

    // Metadatos
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = System.currentTimeMillis(),
    val reactions: Map<String, String> = emptyMap(),
    val isSystemMessage: Boolean = false,

    // Estados
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
) {
    companion object {
        fun create(
            chatId: String,
            senderId: String,
            receiverId: String,
            content: String,
            senderName: String,
            type: MessageType = MessageType.TEXT
        ): Message {
            return Message(
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                senderName = senderName,
                type = type,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun toEdited(newContent: String): Message {
        return copy(
            content = newContent,
            text = newContent,
            isEdited = true,
            timestamp = System.currentTimeMillis()
        )
    }

    fun markAsDeleted(): Message {
        return copy(
            isDeleted = true,
            content = "",
            text = "",
            attachments = emptyList(),
            mentions = emptyList()
        )
    }

    fun updateStatus(newStatus: MessageStatus): Message {
        return copy(status = newStatus)
    }

    fun addReaction(userId: String, reactionType: String): Message {
        return copy(
            reactions = reactions + (userId to reactionType)
        )
    }

    fun removeReaction(userId: String): Message {
        return copy(
            reactions = reactions - userId
        )
    }
}
