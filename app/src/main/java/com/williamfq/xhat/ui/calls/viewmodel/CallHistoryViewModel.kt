package com.williamfq.xhat.ui.calls.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.data.repository.CallRepository
import com.williamfq.xhat.domain.model.CallRecord
import com.williamfq.xhat.domain.model.CallType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val userManager: com.williamfq.data.manager.UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallHistoryUiState())
    val uiState: StateFlow<CallHistoryUiState> = _uiState.asStateFlow()

    init {
        loadCallHistory()
    }

    private fun loadCallHistory() {
        viewModelScope.launch {
            val userId = userManager.getCurrentUserId()
            callRepository.getCallHistory(userId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al cargar el historial"
                    )
                }
                .collect { calls ->
                    _uiState.value = _uiState.value.copy(
                        calls = calls,
                        isLoading = false
                    )
                }
        }
    }

    fun makeCall(userId: String, callType: CallType) {
        viewModelScope.launch {
            try {
                val currentUser = userManager.getCurrentUser()
                val call = CallRecord(
                    callType = callType,
                    callerUserId = currentUser.id,
                    callerUsername = currentUser.username,
                    // Se usa currentUser.username en lugar de currentUser.name
                    callerName = currentUser.username,
                    receiverUserId = userId,
                    startTime = System.currentTimeMillis()
                )
                callRepository.saveCallRecord(call)
                // Iniciar la llamada real (implementar lógica de llamada)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al realizar la llamada"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CallHistoryUiState(
    val calls: List<CallRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
