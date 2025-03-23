package com.williamfq.xhat.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TwoFactorAuthManager {

    suspend fun sendVerificationCode(phoneNumber: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Lógica para enviar el código de verificación
            true
        }
    }

    suspend fun verifyCode(code: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Lógica para verificar el código ingresado
            code == "123456"
        }
    }
}
