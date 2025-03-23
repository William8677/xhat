package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walkie_talkie_audio")
data class WalkieTalkieAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val audioData: ByteArray
)