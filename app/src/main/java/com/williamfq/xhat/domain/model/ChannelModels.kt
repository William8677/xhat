/*
 * Updated: 2025-01-22 01:41:46
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

import java.util.UUID

data class Channel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val createdBy: String,
    val creatorUsername: String,
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val category: ChannelCategory = ChannelCategory.GENERAL,
    val isVerified: Boolean = false,
    val isOfficial: Boolean = false,
    val settings: ChannelSettings = ChannelSettings(),
    val stats: ChannelStats = ChannelStats(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdateAt: Long = System.currentTimeMillis()
)

enum class ChannelCategory {
    GENERAL,
    NEWS,
    ENTERTAINMENT,
    SPORTS,
    TECHNOLOGY,
    BUSINESS,
    EDUCATION,
    LIFESTYLE,
    GAMING,
    MUSIC,
    ART,
    POLITICS,
    SCIENCE
}

data class ChannelSettings(
    val allowReactions: Boolean = true,
    val allowPolls: Boolean = true,
    val allowReplies: Boolean = true,
    val allowSharing: Boolean = true,
    val isPrivate: Boolean = false,
    val requireApproval: Boolean = false,
    val minimumAccountAge: Int = 0, // días
    val minimumKarma: Int = 0,
    val language: String = "es",
    val contentTypes: List<ChannelContentType> = ChannelContentType.values().toList()
)

enum class ChannelContentType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    POLL,
    LINK
}

data class ChannelStats(
    val subscribersCount: Int = 0,
    val postsCount: Int = 0,
    val totalReactions: Int = 0,
    val dailyActiveUsers: Int = 0,
    val weeklyActiveUsers: Int = 0,
    val monthlyActiveUsers: Int = 0,
    val engagement: Double = 0.0,
    val growthRate: Double = 0.0
)

data class ChannelPost(
    val id: String = UUID.randomUUID().toString(),
    val channelId: String,
    val content: String,
    val contentType: ChannelContentType = ChannelContentType.TEXT,
    val attachments: List<ChannelAttachment> = emptyList(),
    val poll: ChannelPoll? = null,
    val reactions: Map<String, Int> = emptyMap(), // emoji -> count
    val viewsCount: Int = 0,
    val sharesCount: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChannelAttachment(
    val type: ChannelContentType,
    val url: String,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val size: Long? = null
)

data class ChannelPoll(
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val endsAt: Long? = null,
    val isMultipleChoice: Boolean = false
)

data class PollOption(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val votes: Int = 0
)

data class ChannelSubscription(
    val channelId: String,
    val userId: String,
    val notificationsEnabled: Boolean = true,
    val subscribedAt: Long = System.currentTimeMillis()
)
