package com.williamfq.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.williamfq.data.converters.StoryConverters

@Entity(tableName = "polls")
@TypeConverters(StoryConverters::class)
data class Poll(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val options: List<String>,
    val creatorId: String,
    val createdAt: Long,
    val isActive: Boolean = true
)