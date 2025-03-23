package com.williamfq.xhat.ui.profile

sealed class ProfileSetupState {
    object Idle : ProfileSetupState()
    object Initial : ProfileSetupState()
    object Loading : ProfileSetupState()
    object Success : ProfileSetupState()
    data class Error(val message: String) : ProfileSetupState()


}
