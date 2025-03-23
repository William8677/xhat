package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Representa una sesión de usuario almacenada en la base de datos.
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID único para cada sesión

    @ColumnInfo(name = "user_id")
    val userId: String, // ID del usuario asociado a la sesión

    @ColumnInfo(name = "session_token")
    val sessionToken: String, // Token único para la sesión

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Marca de tiempo de creación de la sesión

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long, // Marca de tiempo de expiración de la sesión

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true // Indica si la sesión está activa
)
