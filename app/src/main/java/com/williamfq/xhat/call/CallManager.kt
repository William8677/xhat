/*
 * Updated: 2025-02-13 01:55:09
 * Author: William8677
 */

package com.williamfq.xhat.call

import android.content.Context
import android.os.Bundle
import com.williamfq.domain.model.User
import com.williamfq.xhat.events.AppEvent
import com.williamfq.xhat.events.EventBus
import com.williamfq.xhat.service.audio.AudioManager
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.call.model.CallEndReason
import com.williamfq.xhat.call.model.CallErrorCode
import com.williamfq.xhat.utils.analytics.AnalyticsManager

@Singleton
class CallManager @Inject constructor(
    private val context: Context,
    private val eventBus: EventBus,
    private val analytics: AnalyticsManager,
    private val logger: LoggerInterface,
    private val audioManager: AudioManager
) {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> get() = _callState
    private val callScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "CallManager"
    }

    fun startCall() {
        callScope.launch {
            try {
                logger.logEvent(
                    tag = TAG,
                    message = "startCall (sin parámetros) invocado",
                    level = LogLevel.DEBUG
                )

                audioManager.start()
                eventBus.emit(AppEvent.CallStarted)
                _callState.value = CallState.Connecting(
                    User("default", "default_user", "default_avatar_url"),
                    false
                )
                analytics.logEvent("call_started", Bundle())
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error in startCall",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                _callState.value = CallState.Error(
                    code = CallErrorCode.START_FAILED,
                    message = "Failed to start call: ${e.message}"
                )
            }
        }
    }

    fun startCall(
        user: User,
        isVideoCall: Boolean
    ) {
        callScope.launch {
            try {
                logger.logEvent(
                    tag = TAG,
                    message = "startCall(user=${user.username}, video=$isVideoCall) invocado",
                    level = LogLevel.DEBUG
                )

                audioManager.start()
                eventBus.emit(AppEvent.CallStarted)
                _callState.value = CallState.Connecting(user, isVideoCall)

                val params = Bundle().apply {
                    putString("user_id", user.id)
                    putBoolean("is_video", isVideoCall)
                }
                analytics.logEvent("call_started_with_user", params)
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error starting call",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                _callState.value = CallState.Error(
                    code = CallErrorCode.START_FAILED,
                    message = "Failed to start call with user: ${e.message}"
                )
            }
        }
    }

    fun endCall() {
        callScope.launch {
            try {
                logger.logEvent(
                    tag = TAG,
                    message = "endCall invocado",
                    level = LogLevel.INFO
                )

                audioManager.stop()
                eventBus.emit(AppEvent.CallEnded)
                val duration = calculateCallDuration()
                _callState.value = CallState.Ended(
                    CallEndReason.UNKNOWN,
                    duration
                )

                val params = Bundle().apply {
                    putLong("duration", duration)
                }
                analytics.logEvent("call_ended", params)
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error ending call",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                _callState.value = CallState.Error(
                    code = CallErrorCode.END_FAILED,
                    message = "Failed to end call: ${e.message}"
                )
            }
        }
    }

    private fun calculateCallDuration(): Long {
        val startTime = (_callState.value as? CallState.Connected)?.startTime ?: return 0
        return System.currentTimeMillis() - startTime
    }

    fun toggleMute(muted: Boolean) {
        callScope.launch {
            try {
                audioManager.setMuted(muted)
                val params = Bundle().apply {
                    putBoolean("is_muted", muted)
                }
                analytics.logEvent(if (muted) "call_muted" else "call_unmuted", params)
                logger.logEvent(
                    tag = TAG,
                    message = "Audio muted: $muted",
                    level = LogLevel.DEBUG
                )
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error toggling mute",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }

    fun setVolume(volume: Float) {
        callScope.launch {
            try {
                audioManager.setVolume(volume)
                val params = Bundle().apply {
                    putFloat("volume", volume)
                }
                analytics.logEvent("call_volume_changed", params)
                logger.logEvent(
                    tag = TAG,
                    message = "Volume set to: $volume",
                    level = LogLevel.DEBUG
                )
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error setting volume",
                    level = LogLevel.ERROR,
                    throwable = e
                )
            }
        }
    }
}