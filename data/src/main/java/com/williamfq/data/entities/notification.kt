package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una notificación en la base de datos de Xhat.
 */
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID único para cada notificación
    val userId: String, // ID del usuario que recibe la notificación
    val title: String, // Título de la notificación
    val message: String, // Contenido del mensaje
    val timestamp: Long, // Marca de tiempo de la notificación
    val isRead: Boolean = false, // Estado de lectura de la notificación
    val type: String // Tipo de notificación (e.g., mensaje, alerta, promoción)
)
