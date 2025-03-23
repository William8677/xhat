/*
 * Updated: 2025-02-08 00:51:23
 * Author: William8677
 */
package com.williamfq.domain.repository

import com.williamfq.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    suspend fun sendMessage(message: ChatMessage)
    fun getMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun updateMessageStatus(message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun deleteMessage(message: ChatMessage)
    fun getAllMessages(): Flow<List<ChatMessage>>
    suspend fun getMessageById(messageId: String): ChatMessage?
    suspend fun updateMessagesReadStatus(chatId: String, currentUserId: String, isRead: Boolean)
    suspend fun deleteMessagesByChatId(chatId: String)
    fun getLastMessagesFromChats(): Flow<List<ChatMessage>>
}