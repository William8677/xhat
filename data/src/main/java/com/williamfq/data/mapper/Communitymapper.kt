package com.williamfq.data.mapper

import com.williamfq.data.entities.CommunityEntity
import com.williamfq.domain.model.Community

/**
 * Funciones de extensión para mapear entre modelos de datos y de dominio.
 */
fun CommunityEntity.toDomain(): Community = Community(
    id = id.toString(),  // Convertimos Long a String
    name = name,
    description = description,
    createdBy = createdBy,
    createdAt = createdAt,
    isPrivate = isPrivate,
    isActive = isActive
)

fun Community.toEntity(): CommunityEntity = CommunityEntity(
    id = id.toLongOrNull() ?: 0L,  // Convertimos String a Long
    name = name,
    description = description,
    createdBy = createdBy,
    createdAt = createdAt,
    isPrivate = isPrivate,
    isActive = isActive
)
