package com.williamfq.domain.models

/**
 * Representa las estadísticas de actividad de un usuario en la capa de dominio.
 */
data class UserActivityStats(
    val messagesText: Int,  // Total de mensajes enviados
    val callsMade: Int,     // Total de llamadas realizadas
    val lastUpdated: Long   // Última actualización en formato timestamp
)
