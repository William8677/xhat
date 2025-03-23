/*
 * Updated: 2025-01-25 01:48:55
 * Author: William8677
 */

package com.williamfq.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "frequency")
    val frequency: Int = 0,

    @ColumnInfo(name = "last_interaction")
    val lastInteraction: Long = System.currentTimeMillis()
)