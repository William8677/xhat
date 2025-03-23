/*
 * Updated: 2025-02-08 03:39:20
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat.viewmodel

import androidx.lifecycle.ViewModel
import com.williamfq.domain.model.ChatMessage
import com.williamfq.xhat.ui.screens.chat.model.ChatMenuOption
import com.williamfq.xhat.ui.screens.chat.model.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewChatViewModel : ViewModel(), ChatViewModelInterface {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            chatTitle = "Preview Chat",
            messages = listOf(
                ChatMessage(
                    id = 0,
                    messageId = "1",
                    chatId = "preview",
                    content = "Hola",
                    senderId = "user1",
                    recipientId = "user2",
                    timestamp = System.currentTimeMillis(),
                    username = "Usuario de Prueba"
                )
            ),
            isLoading = false,
            currentMessage = ""
        )
    )

    override val uiState: StateFlow<ChatUiState> = _uiState

    override fun loadChat(chatId: String) {}
    override fun onMessageChange(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }
    override fun onSendMessage() {}
    override fun onAttachmentClick() {}
    override fun onMessageClick(message: ChatMessage) {}
    override fun onMessageLongPress(message: ChatMessage) {}
    override fun onMenuClick() {}
    override fun onCallClick() {}
    override fun onVideoCallClick() {}
    override fun onWalkieTalkiePressed() {}
    override fun onWalkieTalkieReleased() {}
    override fun onMenuOptionSelected(option: ChatMenuOption) {}
}
