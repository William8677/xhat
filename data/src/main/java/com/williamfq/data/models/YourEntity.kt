
package com.williamfq.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "your_table")
data class YourEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String
    // Agrega otros campos según sea necesario
)
