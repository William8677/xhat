package com.williamfq.xhat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.location.LocationTracker
import com.williamfq.domain.models.Location
import com.williamfq.domain.models.AlertPriority
import com.williamfq.domain.usecases.SendPanicAlertUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PanicViewModel @Inject constructor(
    private val sendPanicAlertUseCase: SendPanicAlertUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<PanicUiState>(PanicUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    val lastKnownLocation = _lastKnownLocation.asStateFlow()

    init {
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                locationTracker.getLocationUpdates()
                    .collect { location: Location ->
                        _lastKnownLocation.value = location
                    }
            } catch (e: Exception) {
                _uiState.value = PanicUiState.Error("Error al obtener actualizaciones de ubicación")
            }
        }
    }

    fun sendPanicAlert(
        userId: String,
        customMessage: String? = null,
        includeLocation: Boolean = true,
        priority: AlertPriority = AlertPriority.HIGH
    ) {
        viewModelScope.launch {
            _uiState.value = PanicUiState.Sending

            try {
                val location = if (includeLocation) {
                    _lastKnownLocation.value ?: locationTracker.getCurrentLocation().first()
                } else null

                if (location != null) {
                    val message = buildAlertMessage(customMessage, location)
                    sendPanicAlertUseCase.invoke(
                        message = message,
                        userId = userId,
                        location = location,
                        priority = priority
                    )
                    _uiState.value = PanicUiState.Sent
                } else {
                    _uiState.value = PanicUiState.Error("No se pudo obtener la ubicación")
                }
            } catch (e: Exception) {
                _uiState.value = PanicUiState.Error(e.message ?: "Error al enviar alerta")
            }
        }
    }

    private fun buildAlertMessage(customMessage: String?, location: Location): String {
        return buildString {
            append("¡EMERGENCIA! ")
            if (!customMessage.isNullOrBlank()) {
                append(customMessage)
                append(" ")
            } else {
                append("Necesito ayuda inmediata. ")
            }
            append("Mi ubicación actual es: ")
            append("${location.latitude}, ${location.longitude}")
        }
    }

    fun cancelAlert() {
        viewModelScope.launch {
            _uiState.value = PanicUiState.Idle
        }
    }
}

sealed class PanicUiState {
    object Idle : PanicUiState()
    object Sending : PanicUiState()
    object Sent : PanicUiState()
    data class Error(val message: String) : PanicUiState()
}