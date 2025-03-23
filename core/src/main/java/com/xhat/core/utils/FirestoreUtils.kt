
package com.xhat.core.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

object FirestoreUtils {

    private val db = FirebaseFirestore.getInstance()

    fun updateUserActivity(userId: String, field: String, value: Long) {
        val docRef = db.collection("userActivity").document(userId)

        docRef.update(field, FieldValue.increment(value))
            .addOnFailureListener {
                val baseData = mapOf(
                    "messagesText" to 0L,
                    "messagesVoice" to 0L,
                    "callTimeVoice" to 0L,
                    "callTimeVideo" to 0L,
                    "storiesUploaded" to 0L,
                    "topStoryInteractions" to "",
                    "topContact" to "",
                    "firstMessage" to "",
                    "activeDays" to emptyMap<String, Long>(),
                    "activityPercentage" to emptyMap<String, Double>()
                )
                docRef.set(baseData)
            }
    }
}
