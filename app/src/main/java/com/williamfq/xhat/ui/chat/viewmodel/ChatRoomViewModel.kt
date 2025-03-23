/*
 * Updated: 2025-01-22 01:15:42
 * Author: William8677
 */
package com.williamfq.xhat.ui.chat.viewmodel

import android.os.UserManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.data.repository.ChatroomRepository
import com.williamfq.xhat.data.repository.ChatRoomFilter
import com.williamfq.xhat.domain.model.chat.ChatRoom
import com.williamfq.xhat.domain.model.chat.ChatRoomMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatroomRepository: ChatroomRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomUiState())
    val uiState: StateFlow<ChatRoomUiState> = _uiState

    init {
        viewModelScope.launch {
            chatroomRepository.createSystemRooms()
            observeChatRooms()
        }
    }

    private fun observeChatRooms() {
        viewModelScope.launch {
            chatroomRepository.getChatRooms(_uiState.value.filter)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al cargar las salas"
                    )
                }
                .collect { rooms ->
                    _uiState.value = _uiState.value.copy(
                        allRooms = rooms,
                        filteredRooms = filterRooms(rooms, _uiState.value.filter)
                    )
                }
        }
    }

    fun selectCategory(index: Int) {
        val category = com.williamfq.xhat.domain.model.chat.ChatRoomCategory.values()[index]
        _uiState.value = _uiState.value.copy(
            selectedCategoryIndex = index,
            filteredRooms = filterRooms(
                _uiState.value.allRooms,
                _uiState.value.filter.copy(category = category)
            )
        )
    }

    fun updateFilter(filter: ChatRoomFilter) {
        _uiState.value = _uiState.value.copy(
            filter = filter,
            filteredRooms = filterRooms(_uiState.value.allRooms, filter)
        )
    }

    fun selectRoom(room: ChatRoom) {
        _uiState.value = _uiState.value.copy(selectedRoom = room)
    }

    fun clearSelectedRoom() {
        _uiState.value = _uiState.value.copy(selectedRoom = null)
    }

    fun joinRoom(roomId: String) {
        viewModelScope.launch {
            try {
                val member = ChatRoomMember(
                    userId = getCurrentUserId(),
                    username = getCurrentUsername()
                )
                chatroomRepository.joinRoom(roomId, member)
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Error al unirse a la sala"
                )
            }
        }
    }

    private fun filterRooms(rooms: List<ChatRoom>, filter: ChatRoomFilter): List<ChatRoom> {
        return rooms.filter { room ->
            (filter.type == null || room.type == filter.type) &&
                    (filter.category == null || room.category == filter.category) &&
                    (filter.country == null || room.location?.country?.contains(filter.country, ignoreCase = true) == true) &&
                    (filter.city == null || room.location?.city?.contains(filter.city, ignoreCase = true) == true) &&
                    (filter.language == null || room.language.equals(filter.language, ignoreCase = true))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun getCurrentUserId(): String {
        // Dummy: Devuelve un ID de usuario de ejemplo.
        return "1"
    }

    private fun getCurrentUsername(): String {
        // Dummy: Devuelve un nombre de usuario de ejemplo.
        return "demoUser"
    }
}

data class ChatRoomUiState(
    val allRooms: List<ChatRoom> = emptyList(),
    val filteredRooms: List<ChatRoom> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val filter: ChatRoomFilter = ChatRoomFilter(),
    val selectedRoom: ChatRoom? = null,
    val error: String? = null
)
