package com.williamfq.xhat.ui.screens.auth.models

import com.google.firebase.auth.PhoneAuthCredential

sealed class PhoneAuthState {
    object Idle : PhoneAuthState()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneAuthState()
    data class CodeSent(val phoneNumber: String, val verificationId: String) : PhoneAuthState()
    data class Error(val message: String) : PhoneAuthState()
}
