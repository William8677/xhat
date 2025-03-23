package com.williamfq.domain.usecases

import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetChatMessagesUseCase(private val repository: ChatRepository) {
    operator fun invoke(chatId: String): Flow<List<ChatMessage>> {
        return repository.getMessagesByChatId(chatId)
    }
}
