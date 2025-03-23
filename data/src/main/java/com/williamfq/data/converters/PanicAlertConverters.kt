// data/src/main/java/com/williamfq/data/converters/PanicAlertConverters.kt
package com.williamfq.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.williamfq.data.entities.LocationEntity
import com.williamfq.data.entities.PanicAlertStatusEntity

class PanicAlertConverters {

    private val gson = Gson()

    // Convertir LocationEntity a JSON y viceversa
    @TypeConverter
    fun fromLocationEntity(location: LocationEntity?): String? {
        return gson.toJson(location)
    }

    @TypeConverter
    fun toLocationEntity(data: String?): LocationEntity? {
        if (data == null) return null
        val type = object : TypeToken<LocationEntity>() {}.type
        return gson.fromJson(data, type)
    }

    // Convertir PanicAlertStatusEntity a String y viceversa
    @TypeConverter
    fun fromPanicAlertStatus(status: PanicAlertStatusEntity): String {
        return status.name
    }

    @TypeConverter
    fun toPanicAlertStatus(status: String): PanicAlertStatusEntity {
        return try {
            PanicAlertStatusEntity.valueOf(status)
        } catch (e: IllegalArgumentException) {
            PanicAlertStatusEntity.SENDING // Valor por defecto en caso de error
        }
    }
}
