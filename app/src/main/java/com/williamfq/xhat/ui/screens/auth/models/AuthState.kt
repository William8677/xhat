package com.williamfq.xhat.ui.screens.auth.models

import com.google.firebase.auth.PhoneAuthCredential

sealed interface AuthState {
    object Idle : AuthState
    object Initial : AuthState
    object Loading : AuthState
    object Success : AuthState
    data class Error(val message: String) : AuthState
    data class VerificationCompleted(val credential: PhoneAuthCredential) : AuthState
}
