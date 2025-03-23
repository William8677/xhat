/*
 * Updated: 2025-02-13 01:17:41
 * Author: William8677
 */

package com.williamfq.xhat.call.model

enum class CallErrorCode(val message: String) {
    NETWORK_ERROR("Error de red"),
    PERMISSION_DENIED("Permisos denegados"),
    DEVICE_NOT_SUPPORTED("Dispositivo no compatible"),
    SERVICE_UNAVAILABLE("Servicio no disponible"),
    INTERNAL_ERROR("Error interno"),
    INVALID_STATE("Estado inválido"),
    MEDIA_ERROR("Error de medios"),
    REMOTE_ERROR("Error en el dispositivo remoto"),
    CONNECTION_FAILED("Falló la conexión"),
    START_FAILED("Error al iniciar la llamada"),
    END_FAILED("Error al finalizar la llamada"),
    UNKNOWN_ERROR("Error desconocido"),
    GENERIC_ERROR("CallErrorCode");

    fun isRecoverable(): Boolean = when (this) {
        NETWORK_ERROR, SERVICE_UNAVAILABLE, CONNECTION_FAILED, START_FAILED -> true
        else -> false
    }

    fun toAnalyticsString(): String = name.lowercase()

    fun toMap(): Map<String, String> = mapOf(
        "code" to name.lowercase(),
        "message" to message,
        "is_recoverable" to isRecoverable().toString()
    )

    companion object {
        fun fromCode(code: Int): CallErrorCode {
            return when (code) {
                1 -> NETWORK_ERROR
                2 -> PERMISSION_DENIED
                3 -> DEVICE_NOT_SUPPORTED
                4 -> SERVICE_UNAVAILABLE
                5 -> INTERNAL_ERROR
                6 -> INVALID_STATE
                7 -> MEDIA_ERROR
                8 -> REMOTE_ERROR
                9 -> CONNECTION_FAILED
                10 -> START_FAILED
                11 -> END_FAILED
                else -> UNKNOWN_ERROR
            }
        }

        fun fromException(e: Exception): CallErrorCode {
            return when (e) {
                is SecurityException -> PERMISSION_DENIED
                is IllegalStateException -> INVALID_STATE
                is IllegalArgumentException -> START_FAILED
                else -> UNKNOWN_ERROR
            }
        }
    }
}