package com.williamfq.domain.repository

import com.williamfq.domain.model.ChatInfo
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Métodos básicos de mensajería
    fun getOfflineMessages(): Flow<List<ChatMessage>>
    fun getMessagesByChatId(chatId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    suspend fun sendMessage(message: ChatMessage)
    suspend fun getMessagesByChat(chatId: String): List<ChatMessage>

    // Métodos de información del chat
    suspend fun getChatInfo(chatId: String): ChatInfo
    suspend fun getChatMessages(chatId: String): List<ChatMessage>
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun searchMessages(chatId: String, query: String): List<ChatMessage>
    suspend fun getCurrentUserId(): String

    // Métodos de gestión de chat
    suspend fun initiateCall(chatId: String, isVideo: Boolean)
    suspend fun muteChat(chatId: String)
    suspend fun muteChatFor(chatId: String, duration: String)
    suspend fun clearChat(chatId: String)
    suspend fun deleteChat(chatId: String)
    suspend fun blockChat(chatId: String)
    suspend fun exportChat(chatId: String): String
    suspend fun createShortcut(chatId: String)

    // Métodos para Walkie Talkie
    suspend fun sendWalkieTalkieAudio(chatId: String, audioData: ByteArray)
    suspend fun receiveWalkieTalkieAudio(chatId: String): Flow<ByteArray>
    suspend fun notifyWalkieTalkieStart(chatId: String)
    suspend fun notifyWalkieTalkieStop(chatId: String)
    suspend fun editMessage(messageId: String, newContent: String)
    suspend fun editMessageStatus(messageId: String, newStatus: MessageStatus)


}
