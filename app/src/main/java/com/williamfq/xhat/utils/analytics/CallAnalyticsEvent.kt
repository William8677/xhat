/*
 * Updated: 2025-01-27 05:15:56
 * Author: William8677
 */

package com.williamfq.xhat.utils.analytics

import com.williamfq.xhat.call.model.CallStats

sealed class CallAnalyticsEvent : AnalyticsEvent {
    data class CallStarted(
        val userId: String,
        val isVideoCall: Boolean,
        val callId: String
    ) : CallAnalyticsEvent() {
        override val name = "call_started"
        override val parameters = mapOf(
            "user_id" to userId,
            "is_video_call" to isVideoCall,
            "call_id" to callId,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallEnded(
        val duration: Long,
        val endReason: String = END_REASON_USER,
        val bytesTransferred: Long = 0L,
        val quality: String? = null
    ) : CallAnalyticsEvent() {
        override val name = "call_ended"
        override val parameters = mapOf(
            "duration" to duration,
            "end_reason" to endReason,
            "bytes_transferred" to bytesTransferred,
            "quality" to (quality ?: "unknown"),
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallError(
        val errorType: String,
        val errorMessage: String
    ) : CallAnalyticsEvent() {
        override val name = "call_error"
        override val parameters = mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallQualityIssue(
        val issues: List<com.williamfq.xhat.call.model.CallQualityIssue>,
        val stats: CallStats
    ) : CallAnalyticsEvent() {
        override val name = "call_quality_issue"
        override val parameters = mapOf(
            "issues" to issues.map { it.toAnalyticsString() },
            "bitrate" to stats.bitrate,
            "packet_loss" to stats.packetLoss,
            "latency" to stats.latency,
            "resolution" to (stats.resolution ?: "unknown"),
            "duration" to stats.duration,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallQualityWarning(
        val connectionState: String
    ) : CallAnalyticsEvent() {
        override val name = "call_quality_warning"
        override val parameters = mapOf(
            "connection_state" to connectionState,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallAccepted(
        val callId: String,
        val source: String
    ) : CallAnalyticsEvent() {
        override val name = "call_accepted"
        override val parameters = mapOf(
            "call_id" to callId,
            "source" to source,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallRejected(
        val callId: String,
        val source: String
    ) : CallAnalyticsEvent() {
        override val name = "call_rejected"
        override val parameters = mapOf(
            "call_id" to callId,
            "source" to source,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallMissed(
        val callId: String,
        val reason: String
    ) : CallAnalyticsEvent() {
        override val name = "call_missed"
        override val parameters = mapOf(
            "call_id" to callId,
            "reason" to reason,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallRecordingStarted(
        val callId: String
    ) : CallAnalyticsEvent() {
        override val name = "call_recording_started"
        override val parameters = mapOf(
            "call_id" to callId,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallRecordingCompleted(
        val duration: Long
    ) : CallAnalyticsEvent() {
        override val name = "call_recording_completed"
        override val parameters = mapOf(
            "duration" to duration,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallRecordingError(
        val errorType: String,
        val errorMessage: String
    ) : CallAnalyticsEvent() {
        override val name = "call_recording_error"
        override val parameters = mapOf(
            "error_type" to errorType,
            "error_message" to errorMessage,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallRecordingDeleted(
        val filePath: String
    ) : CallAnalyticsEvent() {
        override val name = "call_recording_deleted"
        override val parameters = mapOf(
            "file_path" to filePath,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallMediaToggled(
        val mediaType: String,
        val isEnabled: Boolean
    ) : CallAnalyticsEvent() {
        override val name = "call_media_toggled"
        override val parameters = mapOf(
            "media_type" to mediaType,
            "is_enabled" to isEnabled,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallSwitchCamera(
        val cameraDirection: String
    ) : CallAnalyticsEvent() {
        override val name = "call_switch_camera"
        override val parameters = mapOf(
            "camera_direction" to cameraDirection,
            "timestamp" to System.currentTimeMillis()
        )
    }

    data class CallScreenShare(
        val isStarted: Boolean
    ) : CallAnalyticsEvent() {
        override val name = "call_screen_share"
        override val parameters = mapOf(
            "is_started" to isStarted,
            "timestamp" to System.currentTimeMillis()
        )
    }

    companion object {
        const val END_REASON_USER = "user_ended"
        const val END_REASON_ERROR = "error"
        const val END_REASON_TIMEOUT = "timeout"
        const val END_REASON_REMOTE = "remote_ended"

        const val SOURCE_NOTIFICATION = "notification"
        const val SOURCE_IN_APP = "in_app"
        const val SOURCE_QUICK_ACTIONS = "quick_actions"

        const val MEDIA_TYPE_AUDIO = "audio"
        const val MEDIA_TYPE_VIDEO = "video"
        const val MEDIA_TYPE_SPEAKER = "speaker"

        const val CAMERA_FRONT = "front"
        const val CAMERA_BACK = "back"
    }
}