package com.williamfq.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class SummaryRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Recupera el resumen de actividad del usuario desde Firestore.
     *
     * @param userId ID del usuario.
     * @return Un mapa con los datos de actividad o un mapa vacío si ocurre un error.
     */
    suspend fun getUserActivitySummary(userId: String): Map<String, Any> {
        return try {
            val document = db.collection("userActivity").document(userId).get().await()
            val data = document.data ?: emptyMap()

            mapOf(
                "messages_text" to (data["messages_text"] as? Long ?: 0L).toInt(),
                "calls_made" to (data["calls_made"] as? Long ?: 0L).toInt(),
                "reactions" to (data["reactions"] as? Long ?: 0L).toInt()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el resumen de actividad para userId=$userId")
            emptyMap()
        }
    }
}
