// PollManager.kt: Clase para agregar encuestas en grupos
package com.williamfq.xhat.manager

import com.google.firebase.firestore.FirebaseFirestore

class PollManager {
    private val db = FirebaseFirestore.getInstance()

    fun createPoll(chatId: String, question: String, options: List<String>) {
        val pollData = hashMapOf(
            "question" to question,
            "options" to options,
            "timestamp" to System.currentTimeMillis(),
            "votes" to options.associateWith { 0 }
        )

        db.collection("group_chats").document(chatId).collection("polls").add(pollData)
            .addOnSuccessListener {
                println("Encuesta creada exitosamente: ${it.id}")
            }
            .addOnFailureListener {
                println("Error al crear la encuesta: ${it.message}")
            }
    }

    fun vote(chatId: String, pollId: String, option: String) {
        val pollRef = db.collection("group_chats").document(chatId).collection("polls").document(pollId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(pollRef)
            val votes = snapshot.get("votes") as MutableMap<String, Long>
            votes[option] = votes[option]!! + 1
            transaction.update(pollRef, "votes", votes)
        }.addOnSuccessListener {
            println("Voto registrado exitosamente")
        }.addOnFailureListener {
            println("Error al registrar el voto: ${it.message}")
        }
    }
}