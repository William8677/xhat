// MessageEditor.kt: Clase para gestionar la edición de mensajes enviados
package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore

class MessageEditor {
    private val db = FirebaseFirestore.getInstance()

    fun editMessage(chatId: String, messageId: String, newContent: String, senderId: String) {
        val messageRef = db.collection("chats").document(chatId).collection("messages").document(messageId)

        messageRef.get().addOnSuccessListener { document ->
            if (document.exists() && document.getString("senderId") == senderId) {
                messageRef.update("message", newContent)
                    .addOnSuccessListener {
                        println("Mensaje editado exitosamente: $messageId")
                    }
                    .addOnFailureListener {
                        println("Error al editar el mensaje: ${it.message}")
                    }
            } else {
                println("No se puede editar el mensaje: permisos insuficientes o mensaje no encontrado")
            }
        }.addOnFailureListener {
            println("Error al recuperar el mensaje: ${it.message}")
        }
    }
}