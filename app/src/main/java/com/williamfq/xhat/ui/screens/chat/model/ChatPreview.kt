/*
 * Updated: 2025-02-08 03:32:22
 * Author: William8677
 */

package com.williamfq.xhat.ui.screens.chat.model

data class ChatPreview(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,

    // Avatares y fotos
    val photoUrl: String? = null,
    val avatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val groupAvatarUrls: List<String> = emptyList(), // Para mostrar múltiples avatares en grupos

    // Información de grupo
    val isGroup: Boolean = false,
    val participants: List<String> = emptyList(),
    val participantAvatars: Map<String, String> = emptyMap(), // ID del participante -> URL de avatar
    val adminIds: List<String> = emptyList(),

    // Información del último mensaje
    val lastMessageSenderId: String? = null,
    val lastMessageSenderName: String? = null,
    val lastMessageSenderAvatar: String? = null,

    // Estados y configuración
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,

    // Metadata del avatar
    val avatarLastUpdated: Long? = null,
    val avatarType: AvatarType = AvatarType.DEFAULT,
    val avatarBackground: String? = null,
    val avatarInitials: String? = null,
    val customAvatarEmoji: String? = null
) {
    // Enums para tipos de avatar
    enum class AvatarType {
        DEFAULT,
        CUSTOM_IMAGE,
        INITIALS,
        EMOJI,
        ANIMATED,
        GROUP_GRID,
        DYNAMIC
    }

    // Funciones de utilidad para avatares
    fun getDisplayAvatar(): String? {
        return when {
            avatarUrl != null -> avatarUrl
            photoUrl != null -> photoUrl
            thumbnailUrl != null -> thumbnailUrl
            isGroup && groupAvatarUrls.isNotEmpty() -> groupAvatarUrls.first()
            else -> null
        }
    }

    fun getGroupAvatarGrid(maxAvatars: Int = 4): List<String> {
        return if (isGroup) {
            groupAvatarUrls.take(maxAvatars)
        } else {
            emptyList()
        }
    }

    fun getParticipantAvatar(participantId: String): String? {
        return participantAvatars[participantId]
    }

    fun getInitialsForAvatar(): String {
        return when {
            avatarInitials != null -> avatarInitials
            isGroup -> title.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
            else -> title.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
        }.uppercase()
    }

    // Funciones de comprobación de estado
    fun hasCustomAvatar(): Boolean {
        return avatarUrl != null || photoUrl != null || avatarType != AvatarType.DEFAULT
    }

    fun needsAvatarUpdate(threshold: Long = 86400000): Boolean { // 24 horas en milisegundos
        return avatarLastUpdated?.let { System.currentTimeMillis() - it > threshold } ?: true
    }

    // Función para obtener el color de fondo para avatares basados en iniciales
    fun getAvatarBackgroundColor(): String {
        return avatarBackground ?: generateColorFromTitle()
    }

    private fun generateColorFromTitle(): String {
        val colors = listOf(
            "#FF5733", "#33FF57", "#3357FF", "#FF33F5",
            "#33FFF5", "#F5FF33", "#FF5733", "#33FF57"
        )
        return colors[Math.abs(title.hashCode()) % colors.size]
    }

    // Builder pattern para crear instancias más fácilmente
    class Builder {
        private var id: String = ""
        private var title: String = ""
        private var lastMessage: String = ""
        private var timestamp: Long = 0
        private var unreadCount: Int = 0
        private var photoUrl: String? = null
        private var avatarUrl: String? = null
        private var isGroup: Boolean = false
        private var participants: List<String> = emptyList()
        private var avatarType: AvatarType = AvatarType.DEFAULT

        fun id(id: String) = apply { this.id = id }
        fun title(title: String) = apply { this.title = title }
        fun lastMessage(lastMessage: String) = apply { this.lastMessage = lastMessage }
        fun timestamp(timestamp: Long) = apply { this.timestamp = timestamp }
        fun unreadCount(unreadCount: Int) = apply { this.unreadCount = unreadCount }
        fun photoUrl(photoUrl: String?) = apply { this.photoUrl = photoUrl }
        fun avatarUrl(avatarUrl: String?) = apply { this.avatarUrl = avatarUrl }
        fun isGroup(isGroup: Boolean) = apply { this.isGroup = isGroup }
        fun participants(participants: List<String>) = apply { this.participants = participants }
        fun avatarType(avatarType: AvatarType) = apply { this.avatarType = avatarType }

        fun build() = ChatPreview(
            id = id,
            title = title,
            lastMessage = lastMessage,
            timestamp = timestamp,
            unreadCount = unreadCount,
            photoUrl = photoUrl,
            avatarUrl = avatarUrl,
            isGroup = isGroup,
            participants = participants,
            avatarType = avatarType
        )
    }

    companion object {
        // Factory methods
        fun createGroupPreview(
            id: String,
            title: String,
            participantAvatars: Map<String, String>
        ): ChatPreview {
            return ChatPreview(
                id = id,
                title = title,
                lastMessage = "",
                timestamp = System.currentTimeMillis(),
                isGroup = true,
                participantAvatars = participantAvatars,
                avatarType = AvatarType.GROUP_GRID
            )
        }

        fun createSingleUserPreview(
            id: String,
            title: String,
            avatarUrl: String?
        ): ChatPreview {
            return ChatPreview(
                id = id,
                title = title,
                lastMessage = "",
                timestamp = System.currentTimeMillis(),
                avatarUrl = avatarUrl,
                avatarType = if (avatarUrl != null) AvatarType.CUSTOM_IMAGE else AvatarType.INITIALS
            )
        }
    }
}