package com.williamfq.xhat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.usecases.GetUserIdUseCase
import com.williamfq.domain.usecases.GetOfflineMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la gestión del modo sin conexión
 * Created by William8677 on 2025-01-04 01:52:43
 */
@HiltViewModel
class OfflineModeViewModel @Inject constructor(
    private val getOfflineMessagesUseCase: GetOfflineMessagesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _offlineMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val offlineMessages = _offlineMessages.asStateFlow()

    init {
        loadOfflineMessages()
    }

    fun getCurrentUserId(): String {
        return getUserIdUseCase()
    }

    private fun loadOfflineMessages() {
        viewModelScope.launch {
            getOfflineMessagesUseCase().collect { messages ->
                _offlineMessages.value = messages
            }
        }
    }
}
