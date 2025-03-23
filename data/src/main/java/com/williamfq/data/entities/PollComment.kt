package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poll_comments")
data class PollComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pollId: Int,
    val userId: String,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
