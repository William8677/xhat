// ImportantMessageSecurity.kt: Clase para gestionar la seguridad de mensajes importantes
package com.williamfq.xhat.manager

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class ImportantMessageSecurity(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun protectMessage(chatId: String, messageId: String) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(context as androidx.fragment.app.FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Marcar el mensaje como importante
                    db.collection("chats").document(chatId).collection("messages").document(messageId)
                        .update("important", true)
                        .addOnSuccessListener {
                            println("Mensaje protegido exitosamente: $messageId")
                        }
                        .addOnFailureListener {
                            println("Error al proteger el mensaje: ${it.message}")
                        }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    println("Autenticación biométrica fallida.")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Proteger Mensaje Importante")
            .setSubtitle("Autentíquese para marcar este mensaje como importante")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}