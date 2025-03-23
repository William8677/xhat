
package com.williamfq.domain.repository

import com.williamfq.domain.model.User

interface UserRepository {

    fun getCurrentUserId(): String
    suspend fun saveUserId(userId: String)
    fun isUserLoggedIn(): Boolean
    suspend fun clearUserData()
    suspend fun getCurrentUserName(): String
    suspend fun getCurrentUserEmail(): String
    suspend fun getCurrentUserPhotoUrl(): String
    suspend fun getCurrentUserFollowersCount(): Int
    suspend fun getCurrentUserFollowingCount(): Int
    suspend fun getCurrentUserPostsCount(): Int
    suspend fun getUsers(): List<User>
    suspend fun saveSelectedUsers(selectedUserIds: Set<String>)
    
}
