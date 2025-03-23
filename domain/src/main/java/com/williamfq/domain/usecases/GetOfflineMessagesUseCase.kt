package com.williamfq.domain.usecases

import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener mensajes sin conexión
 * Created by William8677 on 2025-01-04 02:50:25
 */
class GetOfflineMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> {
        return chatRepository.getOfflineMessages()
    }
}