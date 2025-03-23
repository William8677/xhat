package com.williamfq.xhat.ui.stories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.User
import com.williamfq.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la selección de usuarios en "Xhat".
 * Optimiza la UX con selección reactiva, sugerencias inteligentes con Grok y análisis de sentimientos.
 */
@HiltViewModel
class UserSelectionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Estado de los usuarios disponibles
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    // Estado de los usuarios seleccionados (usamos Set para evitar duplicados)
    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers: StateFlow<Set<String>> = _selectedUsers

    // Estado de error para mostrar mensajes al usuario
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUsers()
    }

    /**
     * Carga los usuarios desde el repositorio de manera asíncrona.
     * Maneja errores y actualiza el estado reactivo.
     */
    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val allUsers = userRepository.getUsers()
                _users.value = allUsers
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar usuarios: ${e.message}"
                _users.value = emptyList()
            }
        }
    }

    /**
     * Selecciona un usuario y actualiza el estado.
     * @param userId ID del usuario a seleccionar.
     */
    fun selectUser(userId: String) {
        val currentSelected = _selectedUsers.value.toMutableSet()
        currentSelected.add(userId)
        _selectedUsers.value = currentSelected
    }

    /**
     * Deselecciona un usuario y actualiza el estado.
     * @param userId ID del usuario a deseleccionar.
     */
    fun deselectUser(userId: String) {
        val currentSelected = _selectedUsers.value.toMutableSet()
        currentSelected.remove(userId)
        _selectedUsers.value = currentSelected
    }

    /**
     * Confirma la selección de usuarios y realiza acciones posteriores.
     * Ejemplo: guardar en el repositorio o navegar a otra pantalla.
     */
    fun confirmSelection() {
        viewModelScope.launch {
            try {
                userRepository.saveSelectedUsers(_selectedUsers.value)
                // Podrías emitir un evento de navegación o éxito aquí
            } catch (e: Exception) {
                _errorMessage.value = "Error al confirmar selección: ${e.message}"
            }
        }
    }

    /**
     * Sugiere usuarios inteligentes usando Grok (simulación).
     * En una implementación real, conectaría con la API de Grok.
     * @return Lista de usuarios sugeridos.
     */
    fun suggestUsersWithGrok(): List<User> {
        return users.value.filter { user ->
            user.lastMessage?.contains("hola", ignoreCase = true) == true ||
                    user.name.contains("a", ignoreCase = true)
        }.sortedBy { it.lastActiveTime }
    }

    /**
     * Analiza el sentimiento de un mensaje usando Grok (simulación).
     * En una implementación real, usaría la API de Grok para análisis avanzado.
     * @param message Mensaje a analizar.
     * @return Sentimiento detectado (Positivo, Negativo, Neutral).
     */
    fun analyzeSentimentWithGrok(message: String): String {
        return when {
            message.lowercase().contains("feliz") || message.lowercase().contains("genial") -> "Positivo"
            message.lowercase().contains("triste") || message.lowercase().contains("mal") -> "Negativo"
            else -> "Neutral"
        }
    }

    /**
     * Limpia el mensaje de error después de mostrarlo.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}