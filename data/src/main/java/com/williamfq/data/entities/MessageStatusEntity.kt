// data/src/main/java/com/williamfq/data/entities/MessageStatusEntity.kt
package com.williamfq.data.entities

/**
 * Enum para el estado del mensaje a nivel de base de datos.
 */
enum class MessageStatusEntity {
    SENDING,
    SENT,
    FAILED,
    DELIVERED,
    READ,
    DELETED,
    EDITED,
    AUTO_DESTRUCTED,
    UNKNOWN,
    BLOCKED,
    ERROR

}
