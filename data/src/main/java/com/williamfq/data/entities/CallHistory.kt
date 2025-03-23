package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_history")
data class CallHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // ID del usuario asociado a la llamada
    val callType: String, // Ejemplo: "voice", "video"
    val timestamp: Long, // Marca de tiempo de la llamada
    val duration: Int, // Duración en segundos
    val isMissed: Boolean = false // Si la llamada fue perdida
)
