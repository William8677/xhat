/*
 * Updated: 2025-02-13 00:56:15
 * Author: William8677
 */

package com.williamfq.xhat.call.error

import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallErrorHandler @Inject constructor(
    private val logger: LoggerInterface,
    private val analytics: Analytics
) {
    private val errorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "CallErrorHandler"
    }

    fun handleError(error: CallError) {
        errorScope.launch {
            try {
                logger.logEvent(
                    tag = TAG,
                    message = getErrorMessage(error),
                    level = LogLevel.ERROR,
                    throwable = error.throwable
                )

                analytics.trackEvent(getAnalyticsEventName(error))
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error handling error event",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }

    private fun getErrorMessage(error: CallError): String {
        return when (error) {
            is CallError.ConnectionFailed -> "Call connection failed: ${error.reason}"
            is CallError.MediaError -> "Media error occurred: ${error.description}"
            is CallError.PeerDisconnected -> "Peer disconnected unexpectedly: ${error.peerId}"
            is CallError.SignalingError -> "Signaling error: ${error.message}"
            is CallError.WebRTCError -> "WebRTC error: ${error.description}"
            is CallError.PermissionDenied -> "Permission denied: ${error.permission}"
            is CallError.ICEConnectionFailed -> "ICE connection failed: ${error.description}"
            is CallError.AudioDeviceError -> "Audio device error: ${error.description}"
            is CallError.NetworkError -> "Network error: ${error.description}"
            is CallError.InternalError -> "Internal error occurred: ${error.description}"
        }
    }

    private fun getAnalyticsEventName(error: CallError): String {
        return when (error) {
            is CallError.ConnectionFailed -> "call_connection_failed"
            is CallError.MediaError -> "call_media_error"
            is CallError.PeerDisconnected -> "call_peer_disconnected"
            is CallError.SignalingError -> "call_signaling_error"
            is CallError.WebRTCError -> "call_webrtc_error"
            is CallError.PermissionDenied -> "call_permission_denied"
            is CallError.ICEConnectionFailed -> "call_ice_connection_failed"
            is CallError.AudioDeviceError -> "call_audio_device_error"
            is CallError.NetworkError -> "call_network_error"
            is CallError.InternalError -> "call_internal_error"
        }
    }
}

sealed class CallError(
    open val throwable: Throwable? = null
) {
    data class ConnectionFailed(
        val reason: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class MediaError(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class PeerDisconnected(
        val peerId: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class SignalingError(
        val message: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class WebRTCError(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class PermissionDenied(
        val permission: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class ICEConnectionFailed(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class AudioDeviceError(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class NetworkError(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)

    data class InternalError(
        val description: String,
        override val throwable: Throwable? = null
    ) : CallError(throwable)
}