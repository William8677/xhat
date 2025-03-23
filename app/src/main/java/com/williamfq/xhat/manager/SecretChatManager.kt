package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore
import com.williamfq.xhat.util.EncryptionUtils
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64

class SecretChatManager {
    private val db = FirebaseFirestore.getInstance()
    private val key: SecretKey = EncryptionUtils.generateKey()  // Clave secreta generada
    private val iv: ByteArray = EncryptionUtils.generateIv()   // IV generado de manera aleatoria

    // Método para crear un chat secreto
    fun createSecretChat(chatId: String, userIds: List<String>) {
        val chatData = hashMapOf(
            "chatId" to chatId,
            "userIds" to userIds,
            "timestamp" to System.currentTimeMillis(),
            "isSecret" to true  // Indicador de chat secreto
        )

        db.collection("secret_chats").document(chatId).set(chatData)
            .addOnSuccessListener {
                println("Chat secreto creado exitosamente: $chatId")
            }
            .addOnFailureListener {
                println("Error al crear el chat secreto: ${it.message}")
            }
    }

    // Método para enviar un mensaje secreto
    fun sendSecretMessage(chatId: String, senderId: String, message: String) {
        val encryptedMessage = EncryptionUtils.encrypt(message, key, iv)  // Cifrar mensaje

        val messageData = hashMapOf(
            "senderId" to senderId,
            "message" to encryptedMessage,  // Mensaje cifrado
            "timestamp" to System.currentTimeMillis(),
            "viewed" to false
        )

        db.collection("secret_chats").document(chatId).collection("messages").add(messageData)
            .addOnSuccessListener {
                println("Mensaje secreto enviado exitosamente: ${it.id}")
            }
            .addOnFailureListener {
                println("Error al enviar el mensaje secreto: ${it.message}")
            }
    }

    // Método para leer un mensaje secreto
    fun readSecretMessage(chatId: String, messageId: String) {
        db.collection("secret_chats").document(chatId).collection("messages").document(messageId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val encryptedMessage = document.getString("message") ?: ""
                    val decryptedMessage = EncryptionUtils.decrypt(encryptedMessage, key, iv)  // Descifrar mensaje
                    println("Mensaje leído: $decryptedMessage")

                    // Marcar el mensaje como visto y eliminarlo
                    db.collection("secret_chats").document(chatId).collection("messages").document(messageId)
                        .update("viewed", true)
                        .addOnSuccessListener {
                            deleteSecretMessage(chatId, messageId)
                        }
                }
            }
            .addOnFailureListener {
                println("Error al leer el mensaje secreto: ${it.message}")
            }
    }

    // Método privado para eliminar un mensaje secreto
    private fun deleteSecretMessage(chatId: String, messageId: String) {
        db.collection("secret_chats").document(chatId).collection("messages").document(messageId).delete()
            .addOnSuccessListener {
                println("Mensaje secreto eliminado exitosamente: $messageId")
            }
            .addOnFailureListener {
                println("Error al eliminar el mensaje secreto: ${it.message}")
            }
    }
}
