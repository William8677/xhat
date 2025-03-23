package com.williamfq.xhat.ui.screens.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.williamfq.xhat.ui.screens.auth.models.AuthState
import com.williamfq.xhat.ui.screens.auth.models.VerificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class VerificationCodeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
        Timber.d("VerificationCodeViewModel: Inicializando")
    }

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val verificationId: String? = savedStateHandle["verificationId"]
    private val phoneNumber: String? = savedStateHandle["phoneNumber"]

    fun verifyCode(code: String) {
        Timber.d("VerificationCodeViewModel: Iniciando verificación de código")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (verificationId.isNullOrEmpty()) {
                Timber.e("VerificationCodeViewModel: verificationId es nulo o vacío")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se recibió verificationId."
                )
                _authState.value = AuthState.Error("No se recibió verificationId.")
                return@launch
            }

            try {
                Timber.d("VerificationCodeViewModel: Obteniendo credencial con código")
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                Timber.d("VerificationCodeViewModel: Credencial obtenida, procediendo a sign in")
                signInWithPhoneAuthCredential(credential)
            } catch (e: Exception) {
                Timber.e(e, "VerificationCodeViewModel: Error al verificar código")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al verificar el código"
                )
                _authState.value = AuthState.Error(e.message ?: "Error al verificar el código")
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Timber.d("VerificationCodeViewModel: Iniciando signInWithCredential")
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("VerificationCodeViewModel: Sign in exitoso")
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _authState.value = AuthState.Success
                    }
                } else {
                    Timber.e(task.exception, "VerificationCodeViewModel: Error en sign in")
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = task.exception?.message ?: "Error al verificar el código"
                        )
                        _authState.value = AuthState.Error(
                            task.exception?.message ?: "Error al verificar el código"
                        )
                    }
                }
            }
    }

    fun resendCode(phoneNumber: String, activity: Activity) {
        Timber.d("VerificationCodeViewModel: Iniciando reenvío de código")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            if (this@VerificationCodeViewModel.phoneNumber.isNullOrEmpty()) {
                Timber.e("VerificationCodeViewModel: Número de teléfono no disponible")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Número de teléfono no disponible."
                )
                _authState.value = AuthState.Error("Número de teléfono no disponible.")
                return@launch
            }
            try {
                Timber.d("VerificationCodeViewModel: Configurando opciones de reenvío")
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(this@VerificationCodeViewModel.phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            Timber.d("VerificationCodeViewModel: Verificación automática completada")
                            viewModelScope.launch {
                                _authState.emit(AuthState.Success)
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Timber.e(e, "VerificationCodeViewModel: Verificación fallida")
                            viewModelScope.launch {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = e.message ?: "Error al reenviar el código"
                                )
                                _authState.value = AuthState.Error(e.message ?: "Error al reenviar el código")
                            }
                        }

                        override fun onCodeSent(
                            newVerificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Timber.d("VerificationCodeViewModel: Código reenviado exitosamente")
                            viewModelScope.launch {
                                _authState.emit(AuthState.Idle)
                                _uiState.value = _uiState.value.copy(isLoading = false)
                            }
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                Timber.e(e, "VerificationCodeViewModel: Error al reenviar código")
                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al reenviar el código"
                    )
                    _authState.value = AuthState.Error(e.message ?: "Error al reenviar el código")
                }
            }
        }
    }
}