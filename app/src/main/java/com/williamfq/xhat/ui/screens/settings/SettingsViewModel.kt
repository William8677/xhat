package com.williamfq.xhat.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.ui.screens.settings.model.SettingsGroup
import com.williamfq.xhat.ui.screens.settings.model.SettingItem
import com.williamfq.xhat.ui.screens.settings.model.SubSettingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _settingsGroups = MutableStateFlow<List<SettingsGroup>>(emptyList())
    val settingsGroups: StateFlow<List<SettingsGroup>> = _settingsGroups.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _expandedGroupTitle = MutableStateFlow<String?>(null)
    val expandedGroupTitle: StateFlow<String?> = _expandedGroupTitle.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settingsGroups.value = createSettingsGroups()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterGroups()
    }

    fun onGroupExpand(groupTitle: String) {
        _expandedGroupTitle.value = if (_expandedGroupTitle.value == groupTitle) null else groupTitle
    }

    private fun filterGroups() {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isEmpty()) {
                _settingsGroups.value = createSettingsGroups()
            } else {
                _settingsGroups.value = createSettingsGroups().map { group ->
                    group.copy(
                        items = group.items.filter { item ->
                            item.title.contains(query, ignoreCase = true) ||
                                    item.subItems.any {
                                        it.title.contains(query, ignoreCase = true)
                                    }
                        }
                    )
                }.filter { it.items.isNotEmpty() }
            }
        }
    }

    private fun createSettingsGroups(): List<SettingsGroup> = listOf(
        SettingsGroup(
            title = "Cuenta y Perfil",
            items = listOf(
                SettingItem(
                    title = "Perfil",
                    icon = Icons.Default.Person,
                    route = "settings/profile",
                    subItems = listOf(
                        SubSettingItem("Foto de perfil", "settings/profile/photo"),
                        SubSettingItem("Información personal", "settings/profile/info"),
                        SubSettingItem("Estado y humor", "settings/profile/status"),
                        SubSettingItem("Enlaces y redes sociales", "settings/profile/links"),
                        SubSettingItem("Insignias y logros", "settings/profile/badges"),
                        SubSettingItem("Configuración profesional", "settings/profile/professional")
                    )
                ),
                // ... Resto de los SettingItems igual que en createCompleteSettingsGroups()
            )
        ),
        // ... Resto de los SettingsGroups igual que en createCompleteSettingsGroups()
    )

    fun onSettingItemClick(route: String) {
        _uiState.update { it.copy(lastClickedRoute = route) }
    }

    fun resetLastClickedRoute() {
        _uiState.update { it.copy(lastClickedRoute = null) }
    }

    fun toggleItemExpansion(itemTitle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                expandedItems = currentState.expandedItems.toMutableSet().apply {
                    if (contains(itemTitle)) remove(itemTitle) else add(itemTitle)
                }
            )
        }
    }

    fun isItemExpanded(itemTitle: String): Boolean {
        return _uiState.value.expandedItems.contains(itemTitle)
    }

    data class SettingsUiState(
        val isLoading: Boolean = true,
        val lastClickedRoute: String? = null,
        val expandedItems: Set<String> = emptySet(),
        val error: String? = null
    )

    companion object {
        const val TAG = "SettingsViewModel"
    }
}