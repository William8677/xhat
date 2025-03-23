package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poll_responses")
data class Response(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID único de la respuesta
    val pollId: Int, // ID de la encuesta a la que pertenece la respuesta
    val userId: String, // ID del usuario que respondió
    val selectedOption: Int, // Índice de la opción seleccionada
    val timestamp: Long // Fecha y hora de la respuesta
)
