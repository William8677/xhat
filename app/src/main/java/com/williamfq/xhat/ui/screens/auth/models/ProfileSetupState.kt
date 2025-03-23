package com.williamfq.xhat.ui.screens.auth.models

import android.net.Uri

sealed class ProfileSetupState {
    data object Initial : ProfileSetupState()
    data object Loading : ProfileSetupState()
    data object Success : ProfileSetupState()
    data class Error(val message: String) : ProfileSetupState()
}
