package com.williamfq.data.models

/**
 * Representa estadísticas de actividad de usuario en la capa de datos.
 */
data class UserActivityStats(
    val messagesText: Int,  // Total de mensajes enviados por el usuario
    val callsMade: Int,     // Total de llamadas realizadas por el usuario
    val lastUpdated: Long   // Fecha y hora de la última actividad
)
