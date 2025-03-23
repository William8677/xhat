/*
 * Updated: 2025-01-25 23:18:17
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.domain.model

import java.util.UUID

/**
 * Modelo de dominio que representa una comunidad en la aplicación.
 */
data class Community(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false,
    val isActive: Boolean = true,
    val memberCount: Int = 0,
    val categories: List<String> = emptyList(),
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val rules: List<CommunityRule> = emptyList(),
    val tags: List<String> = emptyList(),
    val isNSFW: Boolean = false,
    val allowCrossPosts: Boolean = true,
    val lastActivityAt: Long = System.currentTimeMillis(),
    val pinnedMessages: List<Long> = emptyList(),
    val moderators: List<String> = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val isSubscribed: Boolean = false,
    val postsCount: Int = 0,
    val activeUsers: Int = 0,
    val ranking: Double = 0.0,
    val isJoined: Boolean = false,
    val settings: CommunitySettings = CommunitySettings()
) {
    /**
     * Comprueba si un usuario es moderador de la comunidad.
     */
    fun isModerator(userId: String): Boolean = userId in moderators

    /**
     * Comprueba si un usuario es el creador de la comunidad.
     */
    fun isCreator(userId: String): Boolean = userId == createdBy

    /**
     * Verifica si un usuario tiene permisos de administración.
     */
    fun hasAdminPermissions(userId: String): Boolean = isCreator(userId) || isModerator(userId)

    companion object {
        const val MAX_NAME_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 1000
        const val MAX_CATEGORIES = 5
        const val MAX_RULES = 20
        const val MAX_MODERATORS = 10
    }
}

/**
 * Configuración de las comunidades.
 */
data class CommunitySettings(
    val allowImages: Boolean = true,
    val allowVideos: Boolean = true,
    val allowLinks: Boolean = true,
    val allowPolls: Boolean = true,
    val requirePostFlair: Boolean = false,
    val restrictPosting: Boolean = false,
    val restrictCommenting: Boolean = false,
    val minimumAccountAge: Int = 0, // en días
    val minimumKarma: Int = 0,
    val requirePostApproval: Boolean = false
)

/**
 * Reglas de la comunidad.
 */
data class CommunityRule(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isRequired: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Estadísticas de la comunidad.
 */
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

/**
 * Miembro de la comunidad.
 */
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

/**
 * Reporte de una comunidad.
 */
data class CommunityReport(
    val id: String = UUID.randomUUID().toString(),
    val reportedContentId: String,
    val reportedBy: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Representa diferentes niveles de visibilidad de una comunidad.
 */
enum class Visibility {
    PUBLIC,       // Visible y accesible para todos
    RESTRICTED,   // Visible, pero con acceso limitado
    PRIVATE       // Solo accesible para miembros invitados
}

/**
 * Roles de los miembros dentro de la comunidad.
 */
enum class MemberRole {
    MEMBER,
    MODERATOR,
    ADMIN
}

/**
 * Tipos de publicaciones dentro de la comunidad.
 */
enum class PostType {
    TEXT,
    IMAGE,
    VIDEO,
    LINK,
    POLL
}

/**
 * Estado de una comunidad en la UI.
 */
sealed class CommunityState {
    object Loading : CommunityState()
    data class Success(val community: Community) : CommunityState()
    data class Error(val message: String) : CommunityState()
}

/**
 * Representa una etiqueta dentro de una comunidad.
 */
data class CommunityFlair(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val backgroundColor: String,
    val textColor: String,
    val icon: String? = null,
    val isModOnly: Boolean = false
)

/**
 * Métodos de utilidad para la comunidad.
 */

/**
 * Devuelve `true` si la comunidad está activa y visible para el usuario.
 */
fun Community.isActive(): Boolean = isActive && (!isPrivate || isSubscribed)

/**
 * Devuelve una representación legible del tiempo transcurrido desde la última actividad.
 */
fun Community.formatLastActivity(): String {
    val now = System.currentTimeMillis()
    val diff = now - lastActivityAt
    return when {
        diff < 60000 -> "Hace un momento"
        diff < 3600000 -> "Hace ${diff / 60000} minutos"
        diff < 86400000 -> "Hace ${diff / 3600000} horas"
        else -> "Hace ${diff / 86400000} días"
    }
}
