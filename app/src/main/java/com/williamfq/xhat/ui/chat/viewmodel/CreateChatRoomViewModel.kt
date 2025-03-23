/*
 * Updated: 2025-01-27 02:00:00
 * Author: William8677
 */
package com.williamfq.xhat.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.data.repository.ChatRoomFilter
import com.williamfq.xhat.domain.model.chat.ChatRoomCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateChatRoomUiState(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null,
    val category: ChatRoomCategory? = null,
    val showCategoryMenu: Boolean = false,
    val maxUsers: Int? = null,
    val isPrivate: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateChatRoomViewModel @Inject constructor(
    // Inyecta aquí las dependencias necesarias (por ejemplo, un repositorio)
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChatRoomUiState())
    val uiState: StateFlow<CreateChatRoomUiState> get() = _uiState

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "El nombre es requerido" else null
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = if (description.isBlank()) "La descripción es requerida" else null
        )
    }

    fun updateCategory(category: ChatRoomCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun toggleCategoryMenu(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(showCategoryMenu = expanded)
    }

    fun updateMaxUsers(max: Int) {
        _uiState.value = _uiState.value.copy(maxUsers = max)
    }

    fun updateIsPrivate(isPrivate: Boolean) {
        _uiState.value = _uiState.value.copy(isPrivate = isPrivate)
    }

    fun createChatRoom() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            delay(2000) // Simulación de llamada de red
            if (_uiState.value.name.isBlank() || _uiState.value.description.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Por favor, complete todos los campos requeridos",
                    isSuccess = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            }
        }
    }
}
