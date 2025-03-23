/*
 * Updated: 2025-02-07 02:07:33
 * Author: William8677
 */
package com.williamfq.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.williamfq.data.entities.DeletionTypeEntity
import com.williamfq.data.entities.MessageAttachmentEntity
import com.williamfq.data.entities.MessageStatusEntity
import com.williamfq.data.entities.MessageTypeEntity

/**
 * Clase de conversión específica para mensajes de chat.
 * Maneja solo las conversiones relacionadas con mensajes.
 */
class ChatMessageConverters {
    private val gson = Gson()

    // Message Type Conversions
    @TypeConverter
    fun fromMessageType(value: MessageTypeEntity): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageTypeEntity =
        runCatching { MessageTypeEntity.valueOf(value) }
            .getOrDefault(MessageTypeEntity.TEXT)

    // Message Status Conversions
    @TypeConverter
    fun fromMessageStatus(value: MessageStatusEntity): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatusEntity =
        runCatching { MessageStatusEntity.valueOf(value) }
            .getOrDefault(MessageStatusEntity.SENDING)

    // Deletion Type Conversions
    @TypeConverter
    fun fromDeletionType(value: DeletionTypeEntity): String = value.name

    @TypeConverter
    fun toDeletionType(value: String): DeletionTypeEntity =
        runCatching { DeletionTypeEntity.valueOf(value) }
            .getOrDefault(DeletionTypeEntity.NONE)

    // Message Attachments Conversions
    @TypeConverter
    fun fromAttachmentList(value: List<MessageAttachmentEntity>): String =
        gson.toJson(value)

    @TypeConverter
    fun toAttachmentList(value: String): List<MessageAttachmentEntity> {
        val listType = object : TypeToken<List<MessageAttachmentEntity>>() {}.type
        return runCatching { gson.fromJson<List<MessageAttachmentEntity>>(value, listType) }
            .getOrDefault(emptyList())
    }

    // Message Specific Map Conversions
    @TypeConverter
    fun fromMessageExtraData(value: Map<String, String>): String =
        gson.toJson(value)

    @TypeConverter
    fun toMessageExtraData(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return runCatching { gson.fromJson<Map<String, String>>(value, mapType) }
            .getOrDefault(emptyMap())
    }

    companion object {
        private const val TRUE_VALUE = 1
        private const val FALSE_VALUE = 0
    }
}