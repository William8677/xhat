package com.williamfq.xhat.ui.screens.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.xhat.ui.main.MainScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _screenState = MutableStateFlow<MainScreens>(MainScreens.CHATS)
    val screenState: StateFlow<MainScreens> = _screenState.asStateFlow()

    private val _isMenuOpen = MutableStateFlow(false)
    val isMenuOpen: StateFlow<Boolean> = _isMenuOpen.asStateFlow()

    fun updateCurrentScreen(page: Int) {
        viewModelScope.launch {
            _screenState.value = MainScreens.entries.getOrNull(page) ?: MainScreens.CHATS
        }
    }

    fun onMenuClick() {
        viewModelScope.launch {
            _isMenuOpen.value = !_isMenuOpen.value
        }
    }

    fun closeMenu() {
        viewModelScope.launch {
            _isMenuOpen.value = false
        }
    }

    fun navigateToScreen(screen: MainScreens) {
        viewModelScope.launch {
            _screenState.value = screen
            closeMenu()
        }
    }
}