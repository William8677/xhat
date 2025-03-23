// EventManager.kt: Clase para gestionar eventos y recordatorios en los chats
package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore

class EventManager {
    private val db = FirebaseFirestore.getInstance()

    fun createEvent(chatId: String, eventName: String, eventTime: Long, reminderTime: Long) {
        val eventData = hashMapOf(
            "eventName" to eventName,
            "eventTime" to eventTime,
            "reminderTime" to reminderTime,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("chats").document(chatId).collection("events").add(eventData)
            .addOnSuccessListener {
                println("Evento creado exitosamente: ${it.id}")
            }
            .addOnFailureListener {
                println("Error al crear el evento: ${it.message}")
            }
    }
}
