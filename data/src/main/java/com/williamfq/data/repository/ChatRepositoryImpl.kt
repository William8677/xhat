package com.williamfq.data.repository

import com.williamfq.data.dao.ChatDao
import com.williamfq.data.entities.MessageEntity
import com.williamfq.data.entities.WalkieTalkieAudioEntity
import com.williamfq.data.mapper.toDomain
import com.williamfq.data.mapper.toEntity
import com.williamfq.domain.model.ChatInfo
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.model.MessageStatus
import com.williamfq.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override suspend fun getChatInfo(chatId: String): ChatInfo = withContext(Dispatchers.IO) {
        chatDao.getChatInfoEntity(chatId).toDomain()
    }

    override suspend fun getChatMessages(chatId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        chatDao.getMessagesByChatIdSync(chatId).map { it.toDomain() }
    }

    override fun observeMessages(): Flow<List<ChatMessage>> =
        chatDao.getOfflineMessages().map { entities -> entities.map { it.toDomain() } }

    // Implementación de getOfflineMessages() como alias de observeMessages()
    override fun getOfflineMessages(): Flow<List<ChatMessage>> = observeMessages()

    // Implementación de getMessagesByChatId() emitiendo una vez los mensajes
    override fun getMessagesByChatId(chatId: String): Flow<List<ChatMessage>> = flow {
        emit(getChatMessages(chatId))
    }

    override suspend fun searchMessages(chatId: String, query: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        chatDao.searchMessages(chatId, query).map { it.toDomain() }
    }

    override suspend fun sendMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message.toEntity())
    }

    override suspend fun getCurrentUserId(): String = withContext(Dispatchers.IO) {
        "current_user_id"
    }

    override suspend fun initiateCall(chatId: String, isVideo: Boolean) {
        TODO("Implementar la lógica de llamada")
    }

    override suspend fun muteChat(chatId: String) {
        TODO("Implementar muteChat")
    }

    override suspend fun muteChatFor(chatId: String, duration: String) {
        TODO("Implementar muteChatFor")
    }

    override suspend fun clearChat(chatId: String) = withContext(Dispatchers.IO) {
        chatDao.clearChat(chatId)
    }

    override suspend fun deleteChat(chatId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteChat(chatId)
    }

    // Implementación de deleteMessage: recibe String, lo convierte a Long
    override suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessageById(messageId.toLong())
    }

    // Implementación de updateMessageStatus: recibe String
    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) = withContext(Dispatchers.IO) {
        chatDao.updateMessageStatusById(messageId.toLong(), status.name)
    }

    // Implementación de saveMessage delegando a sendMessage
    override suspend fun saveMessage(message: ChatMessage) {
        sendMessage(message)
    }

    override suspend fun getMessagesByChat(chatId: String): List<ChatMessage> = getChatMessages(chatId)

    override suspend fun blockChat(chatId: String) {
        TODO("Implementar blockChat")
    }

    override suspend fun exportChat(chatId: String): String = withContext(Dispatchers.IO) {
        "http://export.example.com/chat/$chatId"
    }

    override suspend fun createShortcut(chatId: String) {
        TODO("Implementar createShortcut")
    }

    override suspend fun sendWalkieTalkieAudio(chatId: String, audioData: ByteArray) = withContext(Dispatchers.IO) {
        val entity = WalkieTalkieAudioEntity(chatId = chatId, audioData = audioData)
        chatDao.insertWalkieTalkieAudio(entity)
    }

    override suspend fun receiveWalkieTalkieAudio(chatId: String): Flow<ByteArray> =
        chatDao.getWalkieTalkieAudio(chatId)

    override suspend fun notifyWalkieTalkieStart(chatId: String) {
        TODO("Implementar notifyWalkieTalkieStart")
    }

    override suspend fun notifyWalkieTalkieStop(chatId: String) {
        TODO("Implementar notifyWalkieTalkieStop")
    }

    // Implementación de editMessage
    override suspend fun editMessage(messageId: String, newContent: String) = withContext(Dispatchers.IO) {
        chatDao.editMessage(messageId.toLong(), newContent)
    }

    // Implementación de editMessageStatus
    override suspend fun editMessageStatus(messageId: String, newStatus: MessageStatus) = withContext(Dispatchers.IO) {
        chatDao.editMessageStatus(messageId.toLong(), newStatus.name)
    }
}
