package com.williamfq.data.repository

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.williamfq.domain.model.User
import com.williamfq.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
) : UserRepository {

    override fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: sharedPreferences.getString(KEY_USER_ID, "default_user_id") ?: "default_user_id"
    }

    override suspend fun saveUserId(userId: String) {
        sharedPreferences.edit { putString(KEY_USER_ID, userId) }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null || getCurrentUserId().isNotEmpty()
    }

    override suspend fun clearUserData() {
        sharedPreferences.edit { clear() }
        auth.signOut()
    }

    override suspend fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: sharedPreferences.getString(KEY_USER_NAME, "Usuario Default") ?: "Usuario Default"
    }

    override suspend fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: sharedPreferences.getString(KEY_USER_EMAIL, "default@example.com") ?: "default@example.com"
    }

    override suspend fun getCurrentUserPhotoUrl(): String {
        return auth.currentUser?.photoUrl?.toString() ?: sharedPreferences.getString(KEY_USER_PHOTO_URL, "https://example.com/default-avatar.jpg") ?: "https://example.com/default-avatar.jpg"
    }

    override suspend fun getCurrentUserFollowersCount(): Int {
        return sharedPreferences.getInt("followers_count", 0)
    }

    override suspend fun getCurrentUserFollowingCount(): Int {
        return sharedPreferences.getInt("following_count", 0)
    }

    override suspend fun getCurrentUserPostsCount(): Int {
        return sharedPreferences.getInt("posts_count", 0)
    }

    // Implementaciones de los métodos faltantes
    override suspend fun getUsers(): List<User> {
        // Implementación simulada; en una app real, obtén usuarios de una API o base de datos
        return listOf(
            User(
                id = "1",
                username = "user1",
                avatarUrl = "https://example.com/avatar1.png",
                name = "Usuario 1" // Añade 'name' si es parte de User
            ),
            User(
                id = "2",
                username = "user2",
                avatarUrl = "https://example.com/avatar2.png",
                name = "Usuario 2" // Añade 'name' si es parte de User
            )
        )
    }

    override suspend fun saveSelectedUsers(selectedUserIds: Set<String>) {
        // Guarda los IDs seleccionados en SharedPreferences
        sharedPreferences.edit {
            putStringSet("selected_users", selectedUserIds)
        }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
    }
}