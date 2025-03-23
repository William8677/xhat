/*
 * Updated: 2025-01-22 01:41:46
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.viewmodel

import android.os.UserManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.User
import com.williamfq.xhat.data.repository.ChannelFilter
import com.williamfq.xhat.data.repository.ChannelRepository
import com.williamfq.xhat.domain.model.Channel
import com.williamfq.xhat.domain.model.ChannelAttachment
import com.williamfq.xhat.domain.model.ChannelCategory
import com.williamfq.xhat.domain.model.ChannelContentType
import com.williamfq.xhat.domain.model.ChannelPost
import com.williamfq.xhat.domain.model.ChannelPoll
import com.williamfq.xhat.domain.model.ChannelSubscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelsUiState())
    val uiState: StateFlow<ChannelsUiState> = _uiState

    init {
        loadChannels()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            channelRepository.getChannels(_uiState.value.filter)
                .catch { error: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al cargar canales"
                    )
                }
                .collect { channels: List<Channel> ->
                    _uiState.value = _uiState.value.copy(
                        channels = channels,
                        isLoading = false
                    )
                }
        }
    }

    fun loadPosts(channelId: String) {
        viewModelScope.launch {
            channelRepository.getPosts(channelId)
                .catch { error: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al cargar publicaciones"
                    )
                }
                .collect { posts: List<ChannelPost> ->
                    _uiState.value = _uiState.value.copy(posts = posts)
                }
        }
    }

    fun updateFilter(filter: ChannelFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        loadChannels()
    }

    fun createChannel(
        name: String,
        description: String,
        category: ChannelCategory
    ) {
        viewModelScope.launch {
            try {
                val currentUser = userManager.getCurrentUser()  // Función dummy definida abajo
                val channel = Channel(
                    name = name,
                    description = description,
                    category = category,
                    createdBy = currentUser.id,
                    creatorUsername = currentUser.username,
                    avatarUrl = "", // Se pasa avatarUrl (cadena vacía)
                    coverUrl = ""   // Se pasa coverUrl (cadena vacía)
                )
                channelRepository.createChannel(channel)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al crear el canal"
                )
            }
        }
    }

    fun subscribeToChannel(channelId: String) {
        viewModelScope.launch {
            try {
                val subscription = ChannelSubscription(
                    channelId = channelId,
                    userId = userManager.getCurrentUserId()
                )
                channelRepository.subscribeToChannel(subscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al suscribirse"
                )
            }
        }
    }

    fun createPost(
        channelId: String,
        content: String,
        contentType: ChannelContentType = ChannelContentType.TEXT,
        attachments: List<ChannelAttachment> = emptyList(),
        poll: ChannelPoll? = null
    ) {
        viewModelScope.launch {
            try {
                val post = ChannelPost(
                    channelId = channelId,
                    content = content,
                    contentType = contentType,
                    attachments = attachments,
                    poll = poll
                )
                channelRepository.createPost(channelId, post)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al crear la publicación"
                )
            }
        }
    }

    fun addReactionToPost(channelId: String, postId: String, emoji: String) {
        viewModelScope.launch {
            try {
                channelRepository.addReactionToPost(channelId, postId, emoji)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al agregar reacción"
                )
            }
        }
    }

    fun deletePost(channelId: String, postId: String) {
        viewModelScope.launch {
            try {
                channelRepository.deletePost(channelId, postId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar la publicación"
                )
            }
        }
    }

    fun pinPost(channelId: String, postId: String) {
        viewModelScope.launch {
            try {
                channelRepository.pinPost(channelId, postId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al fijar la publicación"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Implementaciones dummy para obtener el usuario actual.
    private fun UserManager.getCurrentUser(): User {
        // Aquí deberías implementar la lógica real de obtención del usuario.
        return User(id = "1", username = "demoUser", avatarUrl = "")
    }

    fun getCurrentUserId(): String {
        return userManager.getCurrentUser().id
    }

    private fun UserManager.getCurrentUserId(): String {
        return getCurrentUser().id
    }
}

data class ChannelsUiState(
    val channels: List<Channel> = emptyList(),
    val posts: List<ChannelPost> = emptyList(),
    val isLoading: Boolean = true,
    val filter: ChannelFilter = ChannelFilter(),
    val error: String? = null
)
