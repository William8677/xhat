package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_activity",
    indices = [Index(value = ["userId", "period"], unique = true)]
)
data class UserActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "period")
    val period: String,

    @ColumnInfo(name = "messages_text")
    val messagesText: Int = 0,

    @ColumnInfo(name = "messages_voice")
    val messagesVoice: Int = 0,

    @ColumnInfo(name = "messages_deleted")
    val messagesDeleted: Int = 0,

    @ColumnInfo(name = "calls_made")
    val callsMade: Int = 0,

    @ColumnInfo(name = "total_call_duration")
    val totalCallDuration: Long = 0L,

    @ColumnInfo(name = "video_calls")
    val videoCalls: Int = 0,

    @ColumnInfo(name = "total_video_call_duration")
    val totalVideoCallDuration: Long = 0L,

    @ColumnInfo(name = "stories_posted")
    val storiesPosted: Int = 0,

    @ColumnInfo(name = "stories_viewed")
    val storiesViewed: Int = 0,

    @ColumnInfo(name = "reactions_sent")
    val reactionsSent: Int = 0,

    @ColumnInfo(name = "reactions_received")
    val reactionsReceived: Int = 0,

    @ColumnInfo(name = "files_sent")
    val filesSent: Int = 0,

    @ColumnInfo(name = "contacts_added")
    val contactsAdded: Int = 0,

    @ColumnInfo(name = "groups_created")
    val groupsCreated: Int = 0,

    @ColumnInfo(name = "chats_archived")
    val chatsArchived: Int = 0,

    @ColumnInfo(name = "users_blocked")
    val usersBlocked: Int = 0,

    @ColumnInfo(name = "app_opens")
    val appOpens: Int = 0,

    @ColumnInfo(name = "last_activity")
    val lastActivity: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
