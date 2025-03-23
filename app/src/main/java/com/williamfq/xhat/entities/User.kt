package com.williamfq.xhat.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID autogenerado por Room
    val name: String, // Nombre del usuario
    val email: String // Correo electrónico del usuario
)
