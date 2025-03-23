/*
 * Updated: 2025-02-12 16:35:32
 * Author: William8677
 */
package com.williamfq.xhat.utils.user

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.williamfq.domain.models.Location
import com.williamfq.xhat.core.models.Gender
import com.williamfq.xhat.domain.model.*
import com.williamfq.xhat.utils.analytics.AnalyticsManager
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: AnalyticsManager,
    private val logger: LoggerInterface
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    private val _userStatus = MutableStateFlow<UserStatus>(UserStatus.OFFLINE)
    val userStatus: StateFlow<UserStatus> = _userStatus.asStateFlow()

    private val _userPresence = MutableStateFlow<UserPresence>(UserPresence.AWAY)
    val userPresence: StateFlow<UserPresence> = _userPresence.asStateFlow()

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "user_profile_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        scope.launch {
            loadUserProfile()
            startPresenceMonitoring()
        }
    }

    private fun startPresenceMonitoring() {
        scope.launch {
            while (isActive) {
                updatePresence()
                delay(PRESENCE_UPDATE_INTERVAL)
            }
        }
    }

    private suspend fun updatePresence() {
        val currentUser = getCurrentUserId()
        if (currentUser != "anonymous") {
            _userPresence.value = UserPresence.ONLINE
            updateLastSeen()
        }
    }

    suspend fun getCurrentUserId(): String = withContext(Dispatchers.IO) {
        return@withContext auth.currentUser?.uid ?: "anonymous"
    }

    suspend fun getUserProfile(): UserProfile = withContext(Dispatchers.IO) {
        return@withContext _currentProfile.value ?: loadUserProfile()
    }

    private suspend fun loadUserProfile(): UserProfile {
        return try {
            val profileJson = encryptedPrefs.getString(PROFILE_KEY, null)
            val profile = if (profileJson != null) {
                gson.fromJson(profileJson, UserProfile::class.java)
            } else {
                createDefaultProfile()
            }

            _currentProfile.value = profile
            analytics.logEvent("user_profile_loaded", Bundle())
            profile
        } catch (e: Exception) {
            scope.launch {
                logger.logEvent(
                    "UserProfileManager",
                    "Error loading user profile",
                    LogLevel.ERROR,
                    e
                )
            }
            createDefaultProfile()
        }
    }

    private suspend fun createDefaultProfile(): UserProfile {
        val userId = getCurrentUserId()
        return UserProfile(
            id = userId,
            userId = userId,
            username = "",
            phoneNumber = auth.currentUser?.phoneNumber ?: "",
            name = "",
            displayName = "",
            description = "",
            birthDate = "",
            country = "",
            state = "",
            city = "",
            location = Location(),
            gender = Gender.UNSPECIFIED,
            age = 0,
            languages = setOf(),
            behaviors = setOf(),
            profileImageUrl = null,
            coverImageUrl = null,
            status = UserStatus.OFFLINE,
            settings = UserSettings(
                privateProfile = false,
                showLastSeen = true,
                showReadReceipts = true,
                showOnlineStatus = true,
                allowCalls = true,
                allowGroupInvites = true,
                notificationSettings = NotificationSettings()
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        ).also {
            _currentProfile.value = it
            saveProfile(it)
        }
    }

    suspend fun updateProfile(update: (UserProfile) -> UserProfile) {
        withContext(Dispatchers.IO) {
            try {
                val currentProfile = getUserProfile()
                val updatedProfile = update(currentProfile).copy(
                    lastUpdated = System.currentTimeMillis()
                )
                saveProfile(updatedProfile)
                _currentProfile.value = updatedProfile
                analytics.logEvent("user_profile_updated", Bundle())
            } catch (e: Exception) {
                scope.launch {
                    logger.logEvent(
                        "UserProfileManager",
                        "Error updating profile",
                        LogLevel.ERROR,
                        e
                    )
                }
            }
        }
    }

    private fun saveProfile(profile: UserProfile) {
        try {
            val profileJson = gson.toJson(profile)
            encryptedPrefs.edit()
                .putString(PROFILE_KEY, profileJson)
                .putLong(LAST_UPDATE_KEY, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            scope.launch {
                logger.logEvent(
                    "UserProfileManager",
                    "Error saving profile",
                    LogLevel.ERROR,
                    e
                )
            }
        }
    }

    suspend fun updateStatus(status: UserStatus) {
        _userStatus.value = status
        updateProfile { it.copy(status = status) }
    }

    suspend fun updateLastSeen() {
        encryptedPrefs.edit()
            .putLong(LAST_SEEN_KEY, System.currentTimeMillis())
            .apply()
    }

    fun onDestroy() {
        scope.cancel()
    }

    companion object {
        private const val PROFILE_KEY = "user_profile"
        private const val LAST_UPDATE_KEY = "last_update"
        private const val LAST_SEEN_KEY = "last_seen"
        private const val PRESENCE_UPDATE_INTERVAL = 60000L // 1 minuto
    }
}

enum class UserPresence {
    ONLINE,
    AWAY,
    OFFLINE
}