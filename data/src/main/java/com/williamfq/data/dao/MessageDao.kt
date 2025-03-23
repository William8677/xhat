/*
 * Updated: 2025-02-07 00:59:43
 * Author: William8677
 */
package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    /**
     * Obtiene todos los mensajes ordenados por timestamp
     */
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    /**
     * Inserta un mensaje en la base de datos.
     * Reemplaza cualquier mensaje anterior con el mismo primary key.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    /**
     * Actualiza un mensaje existente en la base de datos.
     */
    @Update
    suspend fun updateMessage(message: MessageEntity)

    /**
     * Elimina un mensaje de la base de datos.
     */
    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    /**
     * Recupera todos los mensajes de un chat específico,
     * ordenados por timestamp de manera descendente (el más reciente primero).
     */
    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY timestamp DESC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>

    /**
     * Obtiene un mensaje específico por su ID
     */
    @Query("SELECT * FROM messages WHERE message_id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    /**
     * Actualiza el estado de lectura de los mensajes
     */
    @Query("UPDATE messages SET is_read = :isRead WHERE chat_id = :chatId AND sender_id != :currentUserId")
    suspend fun updateMessagesReadStatus(chatId: String, currentUserId: String, isRead: Boolean)

    /**
     * Actualiza el estado del mensaje
     */
    @Query("UPDATE messages SET message_status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: String)

    /**
     * Elimina todos los mensajes de un chat específico
     */
    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    /**
     * Obtiene el último mensaje de cada chat
     */
    @Query("""
        SELECT * FROM messages 
        WHERE id IN (
            SELECT MAX(id) 
            FROM messages 
            GROUP BY chat_id
        )
        ORDER BY timestamp DESC
    """)
    fun getLastMessagesFromChats(): Flow<List<MessageEntity>>
}