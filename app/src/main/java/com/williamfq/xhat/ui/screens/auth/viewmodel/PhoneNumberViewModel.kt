package com.williamfq.xhat.ui.screens.auth.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.williamfq.xhat.ui.screens.auth.models.PhoneAuthState
import com.williamfq.xhat.ui.screens.auth.models.PhoneNumberUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PhoneNumberViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val authState: StateFlow<PhoneAuthState> = _authState

    var uiState by mutableStateOf(PhoneNumberUiState())
        private set

    // La función recibe la Activity para el flujo de verificación
    fun startPhoneNumberVerification(phoneNumber: String, activity: Activity) {
        if (!isValidPhoneNumber(phoneNumber)) {
            updateUiState(error = "Número de teléfono inválido")
            return
        }

        updateUiState(isLoading = true)

        try {
            val formattedNumber = if (!phoneNumber.startsWith("+")) "+$phoneNumber" else phoneNumber

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)  // Se pasa la Activity válida
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        viewModelScope.launch {
                            _authState.emit(PhoneAuthState.VerificationCompleted(credential))
                            updateUiState(isLoading = false)
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        viewModelScope.launch {
                            _authState.emit(PhoneAuthState.Error(e.message ?: "Error de verificación"))
                            updateUiState(isLoading = false, error = e.message ?: "Error de verificación")
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        viewModelScope.launch {
                            // Emite el verificationId recibido junto al número
                            _authState.emit(PhoneAuthState.CodeSent(formattedNumber, verificationId))
                            updateUiState(isLoading = false)
                        }
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            viewModelScope.launch {
                _authState.emit(PhoneAuthState.Error(e.message ?: "Error desconocido"))
                updateUiState(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^\\+?[1-9]\\d{1,14}$"))
    }

    private fun updateUiState(
        isLoading: Boolean = uiState.isLoading,
        error: String? = null
    ) {
        uiState = uiState.copy(
            isLoading = isLoading,
            error = error
        )
    }
}
