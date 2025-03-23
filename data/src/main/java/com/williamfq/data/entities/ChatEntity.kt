package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.williamfq.domain.model.ChatInfo

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "is_group") val isGroup: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_message_time") val lastMessageTime: Long?,
    @ColumnInfo(name = "unread_count") val unreadCount: Int,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "is_muted") val isMuted: Boolean,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "other_user_id") val otherUserId: String
)
