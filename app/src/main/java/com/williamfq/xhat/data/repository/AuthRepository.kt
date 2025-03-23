package com.williamfq.xhat.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _authState = MutableStateFlow<AuthUser?>(null)
    val authState: StateFlow<AuthUser?> = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser?.let { user ->
                AuthUser(
                    phoneNumber = user.phoneNumber ?: "",
                    verificationId = user.uid
                )
            }
        }
    }

    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ): Flow<AuthResult> = callbackFlow {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(AuthResult.Success)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException ->
                        trySend(AuthResult.InvalidCode)
                    else -> trySend(AuthResult.Error(e.message ?: "Error de verificación"))
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _authState.value = AuthUser(
                    phoneNumber = phoneNumber,
                    verificationId = verificationId
                )
                trySend(AuthResult.Success)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose()
    }

    suspend fun verifyCode(code: String): AuthResult {
        return try {
            val verificationId = _authState.value?.verificationId
                ?: return AuthResult.Error("No hay código de verificación disponible")

            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential).await()

            val user = auth.currentUser ?: return AuthResult.Error("Error de autenticación")
            val userDoc = firestore.collection("users").document(user.uid).get().await()

            if (!userDoc.exists()) {
                val initialProfile = UserProfile(
                    id = user.uid,
                    userId = user.uid,
                    username = "",
                    phoneNumber = user.phoneNumber ?: "",
                    name = "",
                    displayName = "",
                    description = "",
                    birthDate = "",
                    country = "",
                    state = "",
                    city = "",
                    location = Location(),
                    gender = Gender.UNSPECIFIED,
                    age = 0,
                    languages = setOf(),
                    behaviors = setOf(),
                    status = UserStatus.OFFLINE,
                    settings = UserSettings()
                )
                firestore.collection("users").document(user.uid).set(initialProfile).await()
            }

            AuthResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Error verificando código")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> AuthResult.InvalidCode
                else -> AuthResult.Error(e.message ?: "Error de verificación")
            }
        }
    }

    suspend fun setupUserProfile(profile: UserProfile): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("Usuario no autenticado")

            val usernameQuery = firestore.collection("users")
                .whereEqualTo("username", profile.username)
                .get()
                .await()

            if (!usernameQuery.isEmpty) {
                return AuthResult.Error("El nombre de usuario ya está en uso")
            }

            val updatedProfile = profile.copy(
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(updatedProfile)
                .await()

            AuthResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Error configurando perfil")
            when (e) {
                is FirebaseFirestoreException -> AuthResult.Error("Error de base de datos: ${e.message}")
                else -> AuthResult.Error(e.message ?: "Error al configurar el perfil")
            }
        }
    }

    suspend fun signOut() {
        auth.signOut()
        _authState.value = null
    }

    suspend fun getCurrentUser(): UserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo usuario actual")
            null
        }
    }

    suspend fun updateUserStatus(status: UserStatus) {
        val user = auth.currentUser ?: return
        try {
            firestore.collection("users")
                .document(user.uid)
                .update(
                    mapOf(
                        "status" to status,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error actualizando estado del usuario")
        }
    }

    suspend fun updateUserSettings(settings: UserSettings) {
        val user = auth.currentUser ?: return
        try {
            firestore.collection("users")
                .document(user.uid)
                .update(
                    mapOf(
                        "settings" to settings,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error actualizando configuraciones del usuario")
        }
    }

    suspend fun deleteAccount(): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult.Error("Usuario no autenticado")

            firestore.collection("users")
                .document(user.uid)
                .delete()
                .await()

            user.delete().await()
            signOut()
            AuthResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Error eliminando cuenta")
            AuthResult.Error(e.message ?: "Error al eliminar la cuenta")
        }
    }
}