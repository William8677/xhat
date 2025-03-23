/*
 * Updated: 2025-01-26 22:15:00
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

data class AuthUser(
    val phoneNumber: String,
    val verificationId: String,
    val timestamp: Long = System.currentTimeMillis()
)



enum class UserStatus {
    ACTIVE,
    ONLINE,
    AWAY,
    BUSY,
    OFFLINE,
}

data class UserSettings(
    val privateProfile: Boolean = false,
    val showLastSeen: Boolean = true,
    val showReadReceipts: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val allowCalls: Boolean = true,
    val allowGroupInvites: Boolean = true,
    val notificationSettings: NotificationSettings = NotificationSettings()
)

data class NotificationSettings(
    val messageNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object InvalidCode : AuthResult()
    object CodeExpired : AuthResult()
    object NetworkError : AuthResult()
}