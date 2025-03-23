package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reactions")
data class Reaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID único de la reacción

    @ColumnInfo(name = "message_id")
    val messageId: String, // ID del mensaje asociado

    @ColumnInfo(name = "user_id")
    val userId: String, // ID del usuario que reaccionó

    @ColumnInfo(name = "emoji")
    val emoji: String, // Emoji utilizado en la reacción

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis() // Marca de tiempo de la reacción
)
