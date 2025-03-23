// Archivo: presentation/viewmodel/ChatViewModelFactory.kt
package com.williamfq.xhat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.williamfq.domain.repository.ChatRepository
import com.williamfq.domain.usecases.GetChatMessagesUseCase
import com.williamfq.domain.usecases.SendMessageUseCase
import com.williamfq.xhat.presentation.viewmodel.ChatViewModel

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            val getChatMessagesUseCase = GetChatMessagesUseCase(repository)
            val sendMessageUseCase = SendMessageUseCase(repository)
            return ChatViewModel(getChatMessagesUseCase, sendMessageUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
