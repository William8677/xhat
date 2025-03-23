/*
 * Updated: 2025-02-06 20:33:45
 * Author: William8677
 */
package com.williamfq.data.entities

/**
 * Enum para indicar el tipo de borrado de un mensaje en la capa de datos.
 */
enum class DeletionTypeEntity {
    NONE,             // El mensaje no ha sido borrado.
    DELETED_FOR_ME,   // Borrado solo para el usuario actual.
    DELETED_FOR_ALL   // Borrado para todos.
}