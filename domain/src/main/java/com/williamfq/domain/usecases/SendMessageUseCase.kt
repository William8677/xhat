
package com.williamfq.domain.usecases

import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: ChatMessage) {
        repository.sendMessage(message)
    }
}
