package com.williamfq.data.converters

import androidx.room.TypeConverter

class ListConverter {

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")
    }

    @TypeConverter
    fun stringListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}
