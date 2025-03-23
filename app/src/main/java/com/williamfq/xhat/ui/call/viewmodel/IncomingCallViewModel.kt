package com.williamfq.xhat.ui.call.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CallInfo(
    val callerName: String,
    val isVideoCall: Boolean
)

class IncomingCallViewModel : ViewModel() {
    private val _callInfo = MutableStateFlow(CallInfo(callerName = "", isVideoCall = false))
    val callInfo: StateFlow<CallInfo> = _callInfo

    fun loadCallInfo(callId: String) {
        // Simular carga de información de la llamada
        viewModelScope.launch {
            _callInfo.value = CallInfo(callerName = "John Doe", isVideoCall = true)
        }
    }

    fun acceptCall() {
        // Implementar lógica para aceptar la llamada
    }

    fun rejectCall() {
        // Implementar lógica para rechazar la llamada
    }
}