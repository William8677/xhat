// MessageScheduler.kt: Clase para programar mensajes
package com.williamfq.xhat.manager

import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.FirebaseFirestore

class MessageScheduler {
    private val db = FirebaseFirestore.getInstance()

    fun scheduleMessage(chatId: String, senderId: String, message: String, delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            val messageData = hashMapOf(
                "senderId" to senderId,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "viewed" to false
            )

            db.collection("chats").document(chatId).collection("messages").add(messageData)
                .addOnSuccessListener {
                    println("Mensaje programado enviado exitosamente: ${it.id}")
                }
                .addOnFailureListener {
                    println("Error al enviar el mensaje programado: ${it.message}")
                }
        }, delayMillis)
    }
}