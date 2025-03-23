// data/src/main/java/com/williamfq/data/services/ChatService.kt
package com.williamfq.data.services

import javax.inject.Inject

class ChatService @Inject constructor() {
    fun sendMessage(message: String, userId: String, type: String) {
        // Implementa la lógica para enviar un mensaje
        println("Mensaje enviado: $message")
    }
}
