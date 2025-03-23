package com.williamfq.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

data class Story(
    val id: Int,
    val userId: String,
    val title: String,
    val description: String,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.TEXT,
    val timestamp: Long,
    val isActive: Boolean,
    val views: Int,
    val duration: Long? = null, // Añadido para compatibilidad con StoriesViewModel
    val durationHours: Int,
    val durationSeconds: Int = 30,
    val isEncrypted: Boolean = false,
    val encryptionType: EncryptionType? = null,
    val tags: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val poll: Poll? = null,
    val mentions: List<StoryMention> = emptyList(),
    val hashtags: List<StoryHashtag> = emptyList(),
    val interactions: List<StoryInteraction> = emptyList(),
    val highlights: List<StoryHighlight> = emptyList(),
    val hasEffects: Boolean = false,
    val hasInteractiveElements: Boolean = false,
    val privacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val expirationTime: Long? = null,
    val allowedViewers: List<String> = emptyList(),
    val blockedViewers: List<String> = emptyList(),
    val category: StoryCategory = StoryCategory.GENERAL,
    val metadata: StoryMetadata = StoryMetadata(),
    val question: String? = null,
    val options: List<String>? = null,
    val correctAnswer: Int? = null,
    val explanation: String? = null,
    val votes: List<Int>? = null,
    val quizQuestion: String? = null,
    val quizOptions: List<String>? = null,
    val pollQuestion: String? = null,
    val pollOptions: List<String>? = null,
    val backgroundColor: Color? = null,
    val textColor: Color? = null,
    val interactiveElements: List<InteractiveElement>? = null,
    val arInstructions: String? = null
)

data class InteractiveElement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val position: Offset,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
    TEXT,
    AUDIO,
    AR,
    DOCUMENT,
    LOCATION,
    CONTACT,
    POLL,
    QUIZ,
    LINK,
    CUSTOM
}

enum class EncryptionType {
    E2E,
    SERVER_SIDE,
    HYBRID
}

enum class PrivacyLevel {
    PUBLIC,
    PRIVATE,
    FRIENDS,
    CLOSE_FRIENDS,
    CUSTOM
}

enum class StoryCategory {
    GENERAL,
    PERSONAL,
    NEWS,
    ENTERTAINMENT,
    EDUCATION,
    BUSINESS,
    TECH,
    LIFESTYLE,
    TRAVEL,
    FOOD,
    FITNESS,
    MUSIC,
    ART,
    GAMING
}

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mentions: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val isEdited: Boolean = false,
    val editHistory: List<CommentEdit> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val isPrivate: Boolean = false
)

data class CommentEdit(
    val content: String,
    val timestamp: Long,
    val userId: String
)

data class Reaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: ReactionType,
    val content: String = "",
    val isPrivate: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val context: ReactionContext? = null
)

enum class ReactionType {
    LIKE,
    LOVE,
    HAHA,
    WOW,
    SAD,
    ANGRY,
    CARE,
    SUPPORT,
    CELEBRATE,
    CURIOUS,
    THOUGHTFUL,
    CUSTOM,
    FIRE,
    CLAP,
    SMILE,
    LAUGH,
    CRY,
    THINK,
    EYES,
    HUNDRED,
    HEART_EYES,
    MINDBLOWN
}

data class ReactionContext(
    val position: Double? = null,
    val duration: Long? = null,
    val elementId: String? = null
)

data class Poll(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val options: List<PollOption>,
    val votes: Map<String, List<String>> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val expirationTime: Long? = null,
    val isMultipleChoice: Boolean = false,
    val showResults: Boolean = true,
    val allowAddOptions: Boolean = false,
    val metadata: PollMetadata = PollMetadata()
)

data class PollOption(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val imageUrl: String? = null,
    val color: String? = null
)

data class PollMetadata(
    val totalVotes: Int = 0,
    val lastVoteTimestamp: Long? = null,
    val hasUserVoted: Boolean = false,
    val userVoteTimestamp: Long? = null
)

data class StoryInteraction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: InteractionType,
    val timestamp: Long = System.currentTimeMillis(),
    val content: String? = null,
    val duration: Long? = null,
    val metadata: InteractionMetadata = InteractionMetadata()
)

enum class InteractionType {
    VIEW,
    REPLY,
    SHARE,
    SAVE,
    REPORT,
    QUIZ_ANSWER,
    POLL_VOTE,
    AR_INTERACTION,
    LINK_CLICK,
    SWIPE_UP,
    CUSTOM_ACTION
}

data class InteractionMetadata(
    val deviceInfo: DeviceInfo? = null,
    val location: LocationInfo? = null,
    val sessionId: String? = null
)

data class DeviceInfo(
    val deviceId: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String
)

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

data class StoryHighlight(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val coverUrl: String,
    val storyIds: List<Int> = emptyList(),
    val description: String? = null,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class StoryMention(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val username: String,
    val startIndex: Int,
    val endIndex: Int,
    val mentionType: MentionType = MentionType.USER
)

enum class MentionType {
    USER,
    GROUP,
    CHANNEL,
    LOCATION,
    PRODUCT,
    EVENT
}

data class StoryHashtag(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val startIndex: Int,
    val endIndex: Int,
    val trendingScore: Double = 0.0
)

data class Attachment(
    val id: String = UUID.randomUUID().toString(),
    val type: AttachmentType,
    val url: String,
    val thumbnailUrl: String? = null,
    val size: Long? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    GIF,
    STICKER,
    VOICE_MESSAGE
}

data class StoryMetadata(
    val originalQuality: Boolean = false,
    val processingStatus: ProcessingStatus = ProcessingStatus.COMPLETED,
    val viewerSettings: ViewerSettings = ViewerSettings(),
    val analytics: AnalyticsData = AnalyticsData()
)

enum class ProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class ViewerSettings(
    val allowScreenshots: Boolean = true,
    val muteByDefault: Boolean = false,
    val autoplayVideo: Boolean = true,
    val defaultDuration: Int = 30
)

data class AnalyticsData(
    val impressions: Int = 0,
    val uniqueViewers: Int = 0,
    val completionRate: Double = 0.0,
    val averageViewDuration: Double = 0.0,
    val engagementRate: Double = 0.0
)