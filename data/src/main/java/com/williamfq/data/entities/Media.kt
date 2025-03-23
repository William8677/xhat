package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class Media(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val messageId: String, // ID del mensaje asociado
    val mediaType: String, // Ejemplo: "image", "video", "document"
    val filePath: String, // Ruta del archivo almacenado
    val timestamp: Long // Fecha y hora en que se guardó
)
