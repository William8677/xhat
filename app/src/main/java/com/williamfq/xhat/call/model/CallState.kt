/*
 * Updated: 2025-01-27 03:54:45
 * Author: William8677
 */

package com.williamfq.xhat.call.model

import com.williamfq.domain.model.User

sealed class CallState {
    object Idle : CallState()

    data class Ringing(
        val remoteUser: User,
        val callType: CallType,
        val isIncoming: Boolean
    ) : CallState()

    data class Connecting(
        val remoteUser: User,
        val isVideoCall: Boolean,
        val remoteUserJoined: Boolean = false,
        val isReconnecting: Boolean = false
    ) : CallState()

    data class Connected(
        val remoteUser: User,
        val isVideoCall: Boolean,
        val isMuted: Boolean = false,
        val isVideoEnabled: Boolean = true,
        val isRemoteVideoEnabled: Boolean = true,
        val isRemoteMuted: Boolean = false,
        val isScreenSharing: Boolean = false,
        val stats: CallStats? = null,
        val quality: CallQuality = CallQuality.GOOD,
        val startTime: Long = System.currentTimeMillis()
    ) : CallState()

    data class Error(
        val code: CallErrorCode,
        val message: String,
        val isRecoverable: Boolean = false
    ) : CallState()

    data class Ended(
        val reason: CallEndReason,
        val duration: Long,
        val bytesTransferred: Long = 0L,
        val lastQuality: CallQuality = CallQuality.GOOD
    ) : CallState()

    // Renombramos la función para evitar conflictos con los getters generados para la propiedad remoteUser.
    fun getCallRemoteUser(): User? = when (this) {
        is Connected -> remoteUser
        is Connecting -> remoteUser
        is Ringing -> remoteUser
        else -> null
    }

    fun isActive(): Boolean = this is Connected || this is Connecting

    fun isInCall(): Boolean = this !is Idle

    fun toAnalytics(): Map<String, Any> = when (this) {
        is Connected -> mapOf(
            "state" to "connected",
            "remote_user_id" to remoteUser.id,
            "is_video" to isVideoCall,
            "is_muted" to isMuted,
            "is_video_enabled" to isVideoEnabled,
            "is_screen_sharing" to isScreenSharing,
            "quality" to quality.toAnalyticsString(),
            "duration" to (System.currentTimeMillis() - startTime)
        )
        is Connecting -> mapOf(
            "state" to "connecting",
            "remote_user_id" to remoteUser.id,
            "is_video" to isVideoCall,
            "remote_joined" to remoteUserJoined,
            "is_reconnecting" to isReconnecting
        )
        is Error -> mapOf(
            "state" to "error",
            "code" to code.toAnalyticsString(),
            "message" to message,
            "is_recoverable" to isRecoverable
        )
        is Ended -> mapOf(
            "state" to "ended",
            "reason" to reason.toAnalyticsString(),
            "duration" to duration,
            "bytes_transferred" to bytesTransferred,
            "last_quality" to lastQuality.toAnalyticsString()
        )
        is Ringing -> mapOf(
            "state" to "ringing",
            "remote_user_id" to remoteUser.id,
            "call_type" to callType.toAnalyticsString(),
            "is_incoming" to isIncoming
        )
        Idle -> mapOf(
            "state" to "idle"
        )
    }
}
