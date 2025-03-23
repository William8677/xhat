package com.williamfq.xhat.ui.stories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.MediaType
import com.williamfq.domain.model.PrivacyLevel
import com.williamfq.domain.model.Story
import com.williamfq.domain.model.User
import com.williamfq.domain.repository.StoryRepository
import com.williamfq.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers: StateFlow<Set<String>> = _selectedUsers

    private val _storyText = MutableStateFlow<String>("")
    val storyText: StateFlow<String> = _storyText

    private val _sentiment = MutableStateFlow<String>("Neutral")
    val sentiment: StateFlow<String> = _sentiment

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _storySaved = MutableStateFlow<Boolean>(false)
    val storySaved: StateFlow<Boolean> = _storySaved

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val allUsers = userRepository.getUsers()
                _users.value = allUsers
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar usuarios: ${e.message}"
            }
        }
    }

    fun updateStoryText(text: String) {
        _storyText.value = text
        _sentiment.value = analyzeSentimentWithGrok(text)
    }

    fun selectUser(userId: String) {
        val currentSelected = _selectedUsers.value.toMutableSet()
        currentSelected.add(userId)
        _selectedUsers.value = currentSelected
    }

    fun deselectUser(userId: String) {
        val currentSelected = _selectedUsers.value.toMutableSet()
        currentSelected.remove(userId)
        _selectedUsers.value = currentSelected
    }

    fun saveStory() {
        viewModelScope.launch {
            try {
                val story = Story(
                    id = 0,
                    userId = userRepository.getCurrentUserId(),
                    title = _storyText.value,
                    description = _storyText.value,
                    content = _storyText.value,
                    mediaType = MediaType.TEXT,
                    mediaUrl = null,
                    timestamp = System.currentTimeMillis(),
                    isActive = true,
                    views = 0,
                    durationHours = 24,
                    durationSeconds = 30,
                    privacy = PrivacyLevel.PUBLIC,
                    reactions = emptyList(),
                    comments = emptyList(),
                    mentions = emptyList(),
                    hashtags = emptyList(),
                    interactiveElements = null
                )
                storyRepository.addStory(story)
                _storySaved.value = true
                resetState()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar la historia: ${e.message}"
            }
        }
    }

    fun suggestUsersWithGrok(): List<User> {
        return users.value.filter { user ->
            user.lastMessage?.contains("historia", ignoreCase = true) == true ||
                    user.isOnline || user.name.contains("a", ignoreCase = true)
        }.sortedBy { it.lastActiveTime }
    }

    private fun analyzeSentimentWithGrok(message: String): String {
        return when {
            message.lowercase().contains("feliz") || message.lowercase().contains("genial") -> "Positivo"
            message.lowercase().contains("triste") || message.lowercase().contains("mal") -> "Negativo"
            else -> "Neutral"
        }
    }

    private fun generateStoryId(): Int {
        return UUID.randomUUID().hashCode()
    }

    private fun resetState() {
        _storyText.value = ""
        _selectedUsers.value = emptySet()
        _sentiment.value = "Neutral"
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearStorySaved() {
        _storySaved.value = false
    }
}