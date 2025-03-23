// domain/src/main/java/com/williamfq/domain/model/DeletionType.kt
package com.williamfq.domain.model

/**
 * Enum para indicar el tipo de borrado de un mensaje.
 */
enum class DeletionType {
    NONE,             // El mensaje no ha sido borrado.
    DELETED_FOR_ME,   // Borrado solo para el usuario actual.
    DELETED_FOR_ALL   // Borrado para todos.
}
