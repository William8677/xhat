package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // Nombre del canal
    val description: String? = null, // Descripción opcional
    val createdBy: String, // ID del creador del canal
    val membersCount: Int = 1, // Número de miembros del canal
    val createdAt: Long // Fecha de creación
)
