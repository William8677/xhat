/*
 * Updated: 2025-02-06 01:11:11 (modificado para usar ChatMessage)
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat.viewmodel

import kotlinx.coroutines.flow.StateFlow
import com.williamfq.domain.model.ChatMessage
import com.williamfq.xhat.ui.screens.chat.model.ChatMenuOption
import com.williamfq.xhat.ui.screens.chat.model.ChatUiState

interface ChatViewModelInterface {
    val uiState: StateFlow<ChatUiState>

    fun loadChat(chatId: String)
    fun onMessageChange(message: String)
    fun onSendMessage()
    fun onAttachmentClick()
    fun onMessageClick(message: ChatMessage)
    fun onMessageLongPress(message: ChatMessage)
    fun onMenuClick()
    fun onCallClick()
    fun onVideoCallClick()
    fun onWalkieTalkiePressed()
    fun onWalkieTalkieReleased()
    fun onMenuOptionSelected(option: ChatMenuOption)
}
