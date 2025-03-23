package com.williamfq.xhat.ui.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.camera.CameraManager
import com.williamfq.xhat.domain.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
    // Se ha eliminado la inyección directa de FilterViewModel.
    // Para acceder a FilterViewModel, se debe obtener en el Fragment o Activity mediante 'by viewModels()' o 'by activityViewModels()'
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCameraState()
        // Si necesitas observar el estado de filtros, deberás hacerlo en el Fragment que comparte ambos ViewModels.
    }

    private fun observeCameraState() {
        viewModelScope.launch {
            // Se asume que cameraManager.cameraState es un Flow (por ejemplo, un StateFlow)
            cameraManager.cameraState.collect { state: CameraManager.CameraState ->
                _uiState.update { currentState ->
                    when (state) {
                        is CameraManager.CameraState.Preview -> currentState.copy(
                            isPreviewActive = true,
                            error = null
                        )
                        is CameraManager.CameraState.Error -> currentState.copy(
                            error = state.message,
                            isPreviewActive = false
                        )
                        is CameraManager.CameraState.Idle -> currentState.copy(
                            isPreviewActive = false,
                            error = null
                        )
                        else -> currentState // Rama 'else' para garantizar exhaustividad
                    }
                }
            }
        }
    }

    fun startCamera(surface: android.view.Surface, width: Int, height: Int) {
        cameraManager.startCamera(surface, width, height)
    }

    fun stopCamera() {
        cameraManager.stopCamera()
    }

    fun switchCamera() {
        cameraManager.switchCamera()
    }

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
        // TODO: Implementar control real del flash en CameraManager si es necesario
    }

    fun toggleFilter(filter: FilterType?) {
        // La lógica para cambiar de filtro se debe implementar en el Fragment que cuente con el FilterViewModel.
        // Aquí se podría notificar a través de un repositorio o evento.
    }

    fun capturePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            try {
                // TODO: Implementar la captura real de foto
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        lastCaptureSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        lastCaptureSuccess = false,
                        error = "Error al capturar foto: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, filterError = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}

/**
 * Estado principal de la pantalla de cámara.
 */
data class CameraUiState(
    val isPreviewActive: Boolean = false,
    val isCapturing: Boolean = false,
    val isFlashOn: Boolean = false,
    val currentFilter: FilterType? = null,
    val isFilterProcessing: Boolean = false,
    val lastCaptureSuccess: Boolean? = null,
    val error: String? = null,
    val filterError: String? = null
)
