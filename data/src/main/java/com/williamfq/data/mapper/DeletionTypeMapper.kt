/*
 * Updated: 2025-02-06 20:33:45
 * Author: William8677
 */
package com.williamfq.data.mapper

import com.williamfq.data.entities.DeletionTypeEntity
import com.williamfq.domain.model.DeletionType

/**
 * Funciones de extensión para mapear entre DeletionType y DeletionTypeEntity.
 */
fun DeletionType.toEntity(): DeletionTypeEntity =
    when (this) {
        DeletionType.NONE -> DeletionTypeEntity.NONE
        DeletionType.DELETED_FOR_ME -> DeletionTypeEntity.DELETED_FOR_ME
        DeletionType.DELETED_FOR_ALL -> DeletionTypeEntity.DELETED_FOR_ALL
    }

fun DeletionTypeEntity.toDomain(): DeletionType =
    when (this) {
        DeletionTypeEntity.NONE -> DeletionType.NONE
        DeletionTypeEntity.DELETED_FOR_ME -> DeletionType.DELETED_FOR_ME
        DeletionTypeEntity.DELETED_FOR_ALL -> DeletionType.DELETED_FOR_ALL
    }

/**
 * Función de utilidad para mapear de String a DeletionType de forma segura.
 */
fun String?.toDeletionType(): DeletionType =
    when (this?.uppercase()) {
        "NONE" -> DeletionType.NONE
        "DELETED_FOR_ME" -> DeletionType.DELETED_FOR_ME
        "DELETED_FOR_ALL" -> DeletionType.DELETED_FOR_ALL
        else -> DeletionType.NONE
    }

/**
 * Función de utilidad para mapear de String a DeletionTypeEntity de forma segura.
 */
fun String?.toDeletionTypeEntity(): DeletionTypeEntity =
    when (this?.uppercase()) {
        "NONE" -> DeletionTypeEntity.NONE
        "DELETED_FOR_ME" -> DeletionTypeEntity.DELETED_FOR_ME
        "DELETED_FOR_ALL" -> DeletionTypeEntity.DELETED_FOR_ALL
        else -> DeletionTypeEntity.NONE
    }