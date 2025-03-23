package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.williamfq.data.converters.StoryConverters
import com.williamfq.domain.model.*

@Entity(tableName = "stories")
@TypeConverters(StoryConverters::class)
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,
    val content: String = "",
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val views: Int = 0,
    val durationHours: Int = 24,
    val durationSeconds: Int = 30,
    val isEncrypted: Boolean = false,
    val encryptionType: EncryptionType? = null,
    val tags: String = "[]",
    val comments: String = "[]",
    val reactions: String = "[]",
    val poll: String? = null,
    val mentions: String = "[]",
    val hashtags: String = "[]",
    val interactions: String = "[]",
    val highlights: String = "[]",
    val hasEffects: Boolean = false,
    val hasInteractiveElements: Boolean = false,
    val privacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val expirationTime: Long? = null,
    val allowedViewers: String = "[]",
    val blockedViewers: String = "[]",
    val category: StoryCategory = StoryCategory.GENERAL,
    val metadata: String = "{}",
    val analytics: String = "{}",
    val processingStatus: ProcessingStatus = ProcessingStatus.COMPLETED,
    val viewerSettings: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Nuevos campos para funcionalidades avanzadas
    val question: String? = null,
    val options: String? = null,
    val correctAnswer: Int? = null,
    val explanation: String? = null,
    val votes: String? = null,
    val quizQuestion: String? = null,
    val quizOptions: String? = null,
    val pollQuestion: String? = null,
    val pollOptions: String? = null,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val interactiveElements: String? = null,
    val arInstructions: String? = null
)