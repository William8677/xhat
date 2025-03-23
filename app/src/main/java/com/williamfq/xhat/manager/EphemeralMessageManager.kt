// EphemeralMessageManager.kt: Clase para gestionar los mensajes efímeros de voz, video y texto
package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class EphemeralMessageManager : CoroutineScope {
    private val db = FirebaseFirestore.getInstance()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    // Enviar mensaje efímero con duración específica
    fun sendEphemeralMessage(chatId: String, senderId: String, messageType: String, content: String, durationSeconds: Long) {
        val messageData = hashMapOf(
            "senderId" to senderId,
            "messageType" to messageType,  // Texto, Voz, Video
            "content" to content,
            "timestamp" to System.currentTimeMillis(),
            "viewed" to false,
            "oneTimeView" to false  // Indica si es un mensaje de solo vista o no
        )

        db.collection("chats").document(chatId).collection("messages").add(messageData)
            .addOnSuccessListener { documentReference ->
                println("Mensaje efímero enviado exitosamente: ${documentReference.id}")

                // Programar la eliminación del mensaje después de una duración específica
                launch {
                    delay(durationSeconds * 1000) // Retrasar la eliminación según la duración especificada
                    deleteMessage(chatId, documentReference.id)
                }
            }
            .addOnFailureListener {
                println("Error al enviar el mensaje efímero: ${it.message}")
            }
    }

    // Enviar mensaje de "Solo Vista"
    fun sendOneViewMessage(chatId: String, senderId: String, messageType: String, content: String) {
        val messageData = hashMapOf(
            "senderId" to senderId,
            "messageType" to messageType,  // Texto, Voz, Video
            "content" to content,
            "timestamp" to System.currentTimeMillis(),
            "viewed" to false,
            "oneTimeView" to true  // Marca que es un mensaje de "Solo Vista"
        )

        db.collection("chats").document(chatId).collection("messages").add(messageData)
            .addOnSuccessListener { documentReference ->
                println("Mensaje de solo vista enviado exitosamente: ${documentReference.id}")
            }
            .addOnFailureListener {
                println("Error al enviar el mensaje de solo vista: ${it.message}")
            }
    }

    // Función para marcar un mensaje como visto y, si es de solo vista, eliminarlo
    fun markMessageAsViewed(chatId: String, messageId: String) {
        val messageRef = db.collection("chats").document(chatId).collection("messages").document(messageId)

        messageRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val oneTimeView = document.getBoolean("oneTimeView") ?: false
                    val updatedData = hashMapOf<String, Any>("viewed" to true)

                    // Actualizar el estado del mensaje a visto
                    messageRef.update(updatedData)
                        .addOnSuccessListener {
                            if (oneTimeView) {
                                // Si el mensaje es de solo vista, eliminarlo después de ser visto
                                deleteMessage(chatId, messageId)
                            }
                        }
                        .addOnFailureListener {
                            println("Error al actualizar el estado del mensaje: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                println("Error al marcar el mensaje como visto: ${it.message}")
            }
    }

    // Eliminar un mensaje específico de Firestore
    private fun deleteMessage(chatId: String, messageId: String) {
        db.collection("chats").document(chatId).collection("messages").document(messageId).delete()
            .addOnSuccessListener {
                println("Mensaje eliminado exitosamente: $messageId")
            }
            .addOnFailureListener {
                println("Error al eliminar el mensaje: ${it.message}")
            }
    }

    // Cancelar las corrutinas en caso de que se destruya la clase (para evitar fugas de memoria)
    fun clear() {
        job.cancel()
    }
}
