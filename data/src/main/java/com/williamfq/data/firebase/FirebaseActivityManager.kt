
package com.williamfq.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class FirebaseActivityManager {

    private val db = FirebaseFirestore.getInstance()

    fun updateUserActivity(userId: String, field: String, incrementValue: Long) {
        val docRef = db.collection("userActivity").document(userId)
        docRef.update(field, FieldValue.increment(incrementValue))
            .addOnFailureListener {
                // Initialize the document with base fields if it doesn't exist
                val baseData = mapOf(
                    "messagesText" to 0L,
                    "messagesVoice" to 0L,
                    "callTimeVoice" to 0L,
                    "callTimeVideo" to 0L,
                    "storiesUploaded" to 0L,
                    "topStoryInteractions" to "",
                    "topContact" to "",
                    "firstMessage" to ""
                )
                docRef.set(baseData)
            }
    }

    fun getUserActivitySummary(userId: String, onComplete: (Map<String, Any>) -> Unit) {
        db.collection("userActivity").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    onComplete(document.data ?: emptyMap())
                }
            }
            .addOnFailureListener {
                onComplete(emptyMap()) // Return empty data on failure
            }
    }
}
