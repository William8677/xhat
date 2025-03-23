// Archivo: presentation/viewmodel/ChatViewModel.kt
package com.williamfq.xhat.presentation.viewmodel

import androidx.lifecycle.*
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.usecases.GetChatMessagesUseCase
import com.williamfq.domain.usecases.SendMessageUseCase
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                getChatMessagesUseCase(chatId).collect { messages ->
                    _chatMessages.value = messages
                }
            } catch (e: Exception) {
                // Manejar errores adecuadamente
            }
        }
    }

    fun sendMessage(message: ChatMessage) {
        viewModelScope.launch {
            try {
                sendMessageUseCase(message)
                // Opcional: Actualizar la lista de mensajes después de enviar
                loadMessages(message.chatId) // Asegúrate de que ChatMessage tenga un campo chatId
            } catch (e: Exception) {
                // Manejar errores adecuadamente
            }
        }
    }
}
