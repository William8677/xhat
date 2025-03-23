/*
 * Updated: 2025-02-07 02:07:33
 * Author: William8677
 */
package com.williamfq.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converters genéricos para tipos básicos.
 * Maneja conversiones comunes que pueden ser usadas en múltiples entidades.
 */
class Converters {
    private val gson = Gson()

    // Basic List Conversions
    @TypeConverter
    fun fromLongList(list: List<Long>?): String =
        gson.toJson(list ?: emptyList<Long>())

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Generic Boolean Conversions
    @TypeConverter
    fun fromBoolean(value: Boolean): Int = if (value) 1 else 0

    @TypeConverter
    fun toBoolean(value: Int): Boolean = value == 1

    companion object {
        private const val TRUE_VALUE = 1
        private const val FALSE_VALUE = 0
    }
}