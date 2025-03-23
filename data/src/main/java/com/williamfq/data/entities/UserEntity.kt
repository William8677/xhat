package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int, // ID único del usuario
    @ColumnInfo(name = "username") val username: String, // Nombre de usuario
    @ColumnInfo(name = "name") val name: String, // Nombre completo
    @ColumnInfo(name = "email") val email: String, // Correo electrónico
    @ColumnInfo(name = "phone_number") val phoneNumber: String, // Número de teléfono
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String, // URL de imagen de perfil
    @ColumnInfo(name = "status") val status: String, // Estado del usuario (e.g., activo)
    @ColumnInfo(name = "created_at") val createdAt: Long, // Fecha de creación
    @ColumnInfo(name = "last_seen") val lastSeen: Long // Última vez en línea
)
