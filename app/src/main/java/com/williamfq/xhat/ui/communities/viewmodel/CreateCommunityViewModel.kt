/*
 * Updated: 2025-01-26 18:31:15
 * Author: William8677
 */

package com.williamfq.xhat.ui.communities.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreateCommunityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCommunityUiState())
    val uiState: StateFlow<CreateCommunityUiState> = _uiState.asStateFlow()

    fun setBannerImage(url: String) {
        _uiState.update { it.copy(bannerUrl = url) }
    }

    fun setAvatarImage(url: String) {
        _uiState.update { it.copy(avatarUrl = url) }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun setType(type: CommunityType) {
        _uiState.update { it.copy(type = type) }
    }

    fun setRules(rules: List<String>) {
        _uiState.update { it.copy(rules = rules) }
    }

    fun setTags(tags: List<String>) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun setNSFW(isNSFW: Boolean) {
        _uiState.update { it.copy(isNSFW = isNSFW) }
    }

    fun setPrivate(isPrivate: Boolean) {
        _uiState.update { it.copy(isPrivate = isPrivate) }
    }

    fun setAllowCrossPosts(allow: Boolean) {
        _uiState.update { it.copy(allowCrossPosts = allow) }
    }

    fun createCommunity(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        // TODO: Implementar lógica de creación
        onSuccess()
    }
}

data class CreateCommunityUiState(
    val bannerUrl: String = "",
    val avatarUrl: String = "",
    val name: String = "",
    val description: String = "",
    val type: CommunityType = CommunityType.PUBLIC,
    val rules: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isNSFW: Boolean = false,
    val isPrivate: Boolean = false,
    val allowCrossPosts: Boolean = true,
    val isLoading: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && description.isNotBlank()
}

enum class CommunityType(val title: String, val description: String) {
    PUBLIC("Pública", "Cualquiera puede ver y unirse"),
    RESTRICTED("Restringida", "Cualquiera puede ver, pero solo miembros aprobados pueden publicar"),
    PRIVATE("Privada", "Solo miembros aprobados pueden ver y publicar")
}