package com.williamfq.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserActivityRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun incrementMetricSafely(userId: String, period: String, metric: String): Result<Unit> {
        val documentPath = "$userId-$period"
        val docRef = db.collection("userActivity").document(documentPath)

        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(metric, FieldValue.increment(1)).await()
            } else {
                docRef.set(mapOf(metric to 1, "period" to period, "lastUpdated" to System.currentTimeMillis())).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating $metric: ${e.message}")
            Result.failure(e)
        }
    }
}

