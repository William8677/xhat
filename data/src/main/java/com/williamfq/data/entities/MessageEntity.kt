/*
 * Updated: 2025-02-06 21:26:26
 * Author: William8677
 */
package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.williamfq.data.converters.ChatMessageConverters

@Entity(tableName = "messages")
@TypeConverters(ChatMessageConverters::class)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "message_id")
    val messageId: String,

    @ColumnInfo(name = "chat_id")
    val chatId: String,

    @ColumnInfo(name = "sender_id")
    val senderId: String,

    @ColumnInfo(name = "recipient_id")
    val recipientId: String,

    @ColumnInfo(name = "message_content")
    val messageContent: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = true,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "message_type")
    val messageType: MessageTypeEntity = MessageTypeEntity.TEXT,

    @ColumnInfo(name = "message_status")
    val messageStatus: MessageStatusEntity = MessageStatusEntity.SENDING,

    @ColumnInfo(name = "message_attachments")
    val messageAttachments: List<MessageAttachmentEntity> = emptyList(),

    @ColumnInfo(name = "message_extra_data")
    val messageExtraData: Map<String, String> = emptyMap(),

    @ColumnInfo(name = "message_mentions")
    val messageMentions: List<String> = emptyList(),

    @ColumnInfo(name = "message_reply_to")
    val messageReplyTo: String? = null,

    @ColumnInfo(name = "message_room_id")
    val messageRoomId: String? = null,

    @ColumnInfo(name = "message_username")
    val messageUsername: String? = null,

    @ColumnInfo(name = "message_auto_destruct_at")
    val messageAutoDestructAt: Long? = null,

    @ColumnInfo(name = "message_is_edited")
    val messageIsEdited: Boolean = false,

    @ColumnInfo(name = "message_edited_at")
    val messageEditedAt: Long? = null,

    @ColumnInfo(name = "message_deletion_type")
    val messageDeletionType: DeletionTypeEntity = DeletionTypeEntity.NONE,

    @ColumnInfo(name = "message_is_media_message")
    val messageIsMediaMessage: Boolean = false,

    @ColumnInfo(name = "message_can_be_edited")
    val messageCanBeEdited: Boolean = true,

    @ColumnInfo(name = "message_attachment_urls")
    val messageAttachmentUrls: List<String> = emptyList()
)