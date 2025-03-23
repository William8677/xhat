package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 0, // Sólo una fila para configuraciones generales
    val theme: String = "system", // Opciones: "light", "dark", "system"
    val notificationsEnabled: Boolean = true,
    val language: String = "en", // Idioma predeterminado: inglés
    val privacyMode: Boolean = false, // "true" si está activado el modo de privacidad
    val autoSaveMedia: Boolean = true // Si guarda automáticamente fotos/videos
)
