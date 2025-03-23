// BiometricAuthentication.kt: Clase para gestionar la autenticación biométrica
package com.williamfq.xhat.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricAuthentication(private val context: Context) {
    private val biometricManager = BiometricManager.from(context)

    fun canAuthenticate(): Boolean {
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(callback: BiometricPrompt.AuthenticationCallback) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(context as androidx.fragment.app.FragmentActivity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Inicie sesión usando su huella dactilar o reconocimiento facial")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}