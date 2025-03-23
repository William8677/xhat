/*
 * Updated: 2025-01-28 00:07:54
 * Author: William8677
 */

package com.williamfq.xhat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.williamfq.xhat.domain.model.UserProfile
import com.williamfq.xhat.domain.model.UserStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun getCurrentProfile(): UserProfile? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("profiles")
                .document(userId)
                .get()
                .await()

            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val query = db.collection("profiles")
                .whereEqualTo("username", username)
                .get()
                .await()

            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        val currentTime = System.currentTimeMillis()

        // Crear una copia del perfil con los datos actualizados
        val profileData = profile.copy(
            userId = userId,
            lastSeen = currentTime,
            status = UserStatus.ACTIVE
        )

        db.collection("profiles")
            .document(userId)
            .set(profileData)
            .await()
    }

    suspend fun updateLastSeen() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("profiles")
            .document(userId)
            .update(
                mapOf(
                    "lastSeen" to System.currentTimeMillis(),
                    "status" to UserStatus.ACTIVE
                )
            )
            .await()
    }

    suspend fun updateStatus(status: UserStatus) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("profiles")
            .document(userId)
            .update("status", status)
            .await()
    }
}