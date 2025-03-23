/*
 * Updated: 2025-02-08 03:25:26
 * Author: William8677
 */
package com.williamfq.xhat.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import com.williamfq.domain.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.williamfq.domain.model.ChatMessage
import com.williamfq.xhat.domain.model.chat.ChatRoom
import com.williamfq.xhat.ui.screens.chat.model.ChatUiState
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatMessagesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var currentRoomId: String = ""

    fun joinRoom(roomId: String) {
        currentRoomId = roomId
        val dummyRoom = ChatRoom(id = roomId, name = "Sala de Chat $roomId", memberCount = 10)
        _uiState.value = _uiState.value.copy(
            room = dummyRoom,
            currentUserId = "1"
        )
    }

    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val messageContent = _uiState.value.currentMessage.trim()
        if (messageContent.isBlank()) return

        val newMessage = ChatMessage(
            id = 0, // Valor por defecto; el campo 'id' es local (Int)
            messageId = UUID.randomUUID().toString(),
            chatId = currentRoomId,
            senderId = _uiState.value.currentUserId,
            recipientId = _uiState.value.room?.id ?: "",
            content = messageContent,
            username = "Tú",
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
        )

        _uiState.value = _uiState.value.copy(
            messages = listOf(newMessage) + _uiState.value.messages,
            currentMessage = ""
        )
    }
}
