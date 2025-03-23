package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poll_rewards")
data class PollReward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pollId: Int, // ID de la encuesta asociada
    val description: String,
    val value: Double // Puede ser un monto o una métrica específica
)
