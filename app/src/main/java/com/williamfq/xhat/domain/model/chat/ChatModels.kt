/*
 * Updated: 2025-01-22 01:11:41
 * Author: William8677
 */

package com.williamfq.xhat.domain.model.chat

import com.williamfq.domain.model.MessageType

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val type: ChatRoomType = ChatRoomType.USER_CREATED,
    val category: ChatRoomCategory = ChatRoomCategory.GENERAL,
    val location: ChatRoomLocation? = null,
    val language: String = "es",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val memberCount: Int = 0,
    val isActive: Boolean = true,
    val rules: List<String> = emptyList(),
    val moderators: List<String> = emptyList()
)

data class ChatRoomLocation(
    val country: String,
    val region: String,
    val city: String,
    val countryCode: String
)

enum class ChatRoomType {
    SYSTEM_LOCATION,  // Salas por ubicación (país/región/ciudad)
    SYSTEM_LANGUAGE,  // Salas por idioma
    SYSTEM_FRIENDSHIP, // Sala de amistad
    USER_CREATED      // Salas creadas por usuarios
}

enum class ChatRoomCategory {
    GENERAL,
    FRIENDSHIP,
    DATING,
    GAMING,
    MUSIC,
    SPORTS,
    TECHNOLOGY,
    EDUCATION,
    ARTS,
    TRAVEL,
    FOOD,
    LIFESTYLE
}





data class MessageAttachment(
    val type: MessageType,
    val url: String,
    val thumbnailUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null
)

data class ChatRoomMember(
    val userId: String,
    val username: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val isModerator: Boolean = false,
    val isBanned: Boolean = false,
    val isMuted: Boolean = false,
    val muteEndTime: Long? = null
)
