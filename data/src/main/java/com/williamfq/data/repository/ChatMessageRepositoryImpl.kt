/*
 * Updated: 2025-02-08 00:51:23
 * Author: William8677
 */
package com.williamfq.data.repository

import com.williamfq.data.dao.MessageDao
import com.williamfq.data.mapper.toDomain
import com.williamfq.data.mapper.toEntity
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.repository.ChatMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementación del repositorio de mensajes de chat que utiliza Room como fuente de datos.
 *
 * Esta implementación aprovecha las capacidades reactivas de Flow para proporcionar
 * actualizaciones en tiempo real de los mensajes, lo cual es crucial para una aplicación
 * de chat. El uso de Flow permite:
 * - Actualizaciones en tiempo real de la UI
 * - Manejo eficiente de recursos
 * - Integración natural con Kotlin Coroutines
 */
class ChatMessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : ChatMessageRepository {

    /**
     * Envía un nuevo mensaje al chat.
     * La operación se realiza en el contexto de IO para no bloquear el hilo principal.
     */
    override suspend fun sendMessage(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            val entity = message.toEntity()
            messageDao.insertMessage(entity)
        }
    }

    /**
     * Obtiene un Flow de mensajes para un chat específico.
     * Los mensajes se actualizarán automáticamente cuando haya cambios en la base de datos.
     */
    override fun getMessages(chatId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesByChatId(chatId)
            .map { messages ->
                messages.map { messageEntity ->
                    messageEntity.toDomain()
                }
            }
    }

    /**
     * Actualiza el estado de un mensaje existente.
     * Utiliza el método updateMessageStatus del DAO que espera un ID y un estado.
     */
    override suspend fun updateMessageStatus(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            val entity = message.toEntity()
            messageDao.updateMessageStatus(
                messageId = entity.id,
                status = entity.messageStatus.name
            )
        }
    }

    /**
     * Actualiza un mensaje existente en la base de datos.
     */
    override suspend fun updateMessage(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            messageDao.updateMessage(message.toEntity())
        }
    }

    /**
     * Elimina un mensaje de la base de datos.
     */
    override suspend fun deleteMessage(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            messageDao.deleteMessage(message.toEntity())
        }
    }

    /**
     * Obtiene todos los mensajes ordenados por timestamp.
     */
    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return messageDao.getAllMessages()
            .map { messages -> messages.map { it.toDomain() } }
    }

    /**
     * Obtiene un mensaje específico por su ID.
     */
    override suspend fun getMessageById(messageId: String): ChatMessage? {
        return withContext(Dispatchers.IO) {
            messageDao.getMessageById(messageId)?.toDomain()
        }
    }

    /**
     * Actualiza el estado de lectura de los mensajes de un chat.
     */
    override suspend fun updateMessagesReadStatus(
        chatId: String,
        currentUserId: String,
        isRead: Boolean
    ) {
        withContext(Dispatchers.IO) {
            messageDao.updateMessagesReadStatus(chatId, currentUserId, isRead)
        }
    }

    /**
     * Elimina todos los mensajes de un chat específico.
     */
    override suspend fun deleteMessagesByChatId(chatId: String) {
        withContext(Dispatchers.IO) {
            messageDao.deleteMessagesByChatId(chatId)
        }
    }

    /**
     * Obtiene el último mensaje de cada chat.
     */
    override fun getLastMessagesFromChats(): Flow<List<ChatMessage>> {
        return messageDao.getLastMessagesFromChats()
            .map { messages -> messages.map { it.toDomain() } }
    }

    companion object {
        private const val TAG = "ChatMessageRepositoryImpl"
    }
}