/*
 * Updated: 2025-01-25 23:18:17
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.domain.model

import com.williamfq.domain.model.User
import com.williamfq.xhat.ui.communities.viewmodel.CommunityType
import java.util.UUID



data class CommunityStatistics(
    val postsCount: Int = 0,
    val commentsCount: Int = 0,
    val activeUsers: Int = 0,
    val ranking: Double = 0.0,
    val growthRate: Double = 0.0,
    val engagementRate: Double = 0.0,
    val topContributors: List<User> = emptyList(),
    val postDistribution: Map<PostType, Int> = emptyMap()
)


data class CommunityMember(
    val userId: String,
    val communityId: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val role: MemberRole = MemberRole.MEMBER,
    val karma: Int = 0,
    val isApproved: Boolean = false,
    val isBanned: Boolean = false,
    val banReason: String? = null,
    val banExpiresAt: Long? = null
)

enum class MemberRole {
    MEMBER,
    MODERATOR,
    ADMIN
}

enum class PostType {
    TEXT,
    IMAGE,
    VIDEO,
    LINK,
    POLL
}

data class CommunityFlair(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val backgroundColor: String,
    val textColor: String,
    val icon: String? = null,
    val isModOnly: Boolean = false
)

data class CommunityReport(
    val id: String = UUID.randomUUID().toString(),
    val reportedContentId: String,
    val reportedBy: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
