// UserStatisticsManager.kt: Clase para gestionar estadísticas de uso
package com.williamfq.xhat.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserStatisticsManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun updateMessageSentCount() {
        val currentUser = auth.currentUser ?: return
        val statsRef = db.collection("users").document(currentUser.uid).collection("statistics").document("usageStats")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(statsRef)
            val messageCount = snapshot.getLong("messagesSent") ?: 0
            transaction.update(statsRef, "messagesSent", messageCount + 1)
        }
    }

    fun getStatistics(callback: (Map<String, Any>?) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val statsRef = db.collection("users").document(currentUser.uid).collection("statistics").document("usageStats")

        statsRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.data)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
