/*
 * Updated: 2025-01-22 00:53:55
 * Author: William8677
 */

package com.williamfq.xhat.ui.communities.filters

import com.williamfq.xhat.domain.model.PostType

// Nombre del archivo: CommunitySearchFilters.kt
// Ruta: src/main/java/com/williamfq/xhat/ui/communities/filters/CommunitySearchFilters.kt

data class CommunitySearchFilters(
    val query: String = "",
    val sortBy: CommunitySortOption = CommunitySortOption.TRENDING,
    val categoryFilter: List<CommunityCategory> = emptyList(),
    val memberCountRange: ClosedRange<Int>? = null,
    val ageRange: ClosedRange<Long>? = null,
    val contentTypeFilter: List<PostType> = emptyList(),
    val showNSFW: Boolean = false,
    val showPrivate: Boolean = true,
    val onlyJoined: Boolean = false,
    val onlyModerated: Boolean = false,
    val language: String? = null,
    val activeInLast: Long? = null // en milisegundos
)

enum class CommunitySortOption(val title: String) {
    TRENDING("Tendencias"),
    NEWEST("Más nuevas"),
    MOST_MEMBERS("Más miembros"),
    MOST_ACTIVE("Más activas"),
    ALPHABETICAL("Alfabético")
}

enum class CommunityCategory(val title: String) {
    GAMING("Gaming"),
    TECHNOLOGY("Tecnología"),
    SCIENCE("Ciencia"),
    ENTERTAINMENT("Entretenimiento"),
    SPORTS("Deportes"),
    ARTS("Arte"),
    MUSIC("Música"),
    EDUCATION("Educación"),
    LIFESTYLE("Estilo de vida"),
    NEWS("Noticias"),
    OTHER("Otros")
}