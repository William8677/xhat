// domain/src/main/java/com/williamfq/domain/model/MessageType.kt
package com.williamfq.domain.model

/**
 * Enum para el tipo de mensaje en la capa de dominio.
 */
enum class MessageType {
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
    DOCUMENT,

}
