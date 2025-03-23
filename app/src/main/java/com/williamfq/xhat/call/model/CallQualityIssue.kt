/*
 * Updated: 2025-01-27 04:38:45
 * Author: William8677
 */

package com.williamfq.xhat.call.model

enum class CallQualityIssue(val userMessage: String) {
    HIGH_LATENCY("Conexión lenta detectada"),
    PACKET_LOSS("Problemas de conexión detectados"),
    LOW_BITRATE("Calidad de video reducida");

    fun toAnalyticsString(): String = name.lowercase()

    fun getRecommendation(): String = when (this) {
        HIGH_LATENCY -> "Intenta acercarte al router o usar una red WiFi"
        PACKET_LOSS -> "Verifica tu conexión a internet"
        LOW_BITRATE -> "La calidad del video se ha reducido para mantener la llamada"
    }

    companion object {
        fun getNotificationPriority(issue: CallQualityIssue): Int = when (issue) {
            PACKET_LOSS -> 3    // Alta prioridad
            HIGH_LATENCY -> 2   // Media prioridad
            LOW_BITRATE -> 1    // Baja prioridad
        }
    }
}