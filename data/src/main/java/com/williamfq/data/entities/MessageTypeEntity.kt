// data/src/main/java/com/williamfq/data/entities/MessageTypeEntity.kt
package com.williamfq.data.entities

/**
 * Enum para el tipo de mensaje a nivel de base de datos.
 */
enum class MessageTypeEntity {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    STICKER,
    SYSTEM,
    VOICE_NOTE,
    LOCATION,
    CONTACT,
    GIF,
    POLL,
    DOCUMENT
}
