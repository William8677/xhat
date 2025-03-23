/*
 * Updated: 2025-01-27 01:15:54
 * Author: William8677
 */

package com.williamfq.domain.model

data class ChatInfo(
    val id: String,
    val title: String,
    val photoUrl: String? = null,
    val isGroup: Boolean = false,
    val participants: List<Participant> = emptyList(),
    val createdAt: Long,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val otherUserId: String,
)

data class Participant(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val role: ParticipantRole = ParticipantRole.MEMBER,
    val lastSeen: Long? = null
)

enum class ParticipantRole {
    OWNER,
    ADMIN,
    MEMBER
}