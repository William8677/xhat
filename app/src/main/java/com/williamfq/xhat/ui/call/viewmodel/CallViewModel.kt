/*
 * Updated: 2025-02-13 01:06:14
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.User
import com.williamfq.xhat.call.CallManager
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.domain.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val analytics: Analytics,
    private val logger: LoggerInterface
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    val callState: StateFlow<CallState> = callManager.callState

    private val _currentFilter = MutableStateFlow<Filter?>(null)
    val currentFilter: StateFlow<Filter?> = _currentFilter.asStateFlow()

    private val _showGameSelector = MutableStateFlow(false)
    val showGameSelector: StateFlow<Boolean> = _showGameSelector.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOn = MutableStateFlow(true)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    companion object {
        private const val TAG = "CallViewModel"
    }

    init {
        observeCallState()
    }

    private fun observeCallState() {
        viewModelScope.launch {
            callManager.callState.collect { state ->
                _uiState.update { currentState ->
                    when (state) {
                        is CallState.Connected -> currentState.copy(
                            isConnected = true,
                            isConnecting = false,
                            error = null,
                            isMuted = state.isMuted,
                            isVideoEnabled = state.isVideoEnabled,
                            remoteUser = state.remoteUser,
                            callStartTime = state.startTime
                        )
                        is CallState.Connecting -> currentState.copy(
                            isConnecting = true,
                            isConnected = false,
                            error = null,
                            remoteUser = state.remoteUser
                        )
                        is CallState.Error -> {
                            logger.logEvent(
                                tag = TAG,
                                message = "Call error: ${state.message}",
                                level = LogLevel.ERROR
                            )
                            currentState.copy(
                                error = state.message,
                                isConnecting = false,
                                isConnected = false
                            )
                        }
                        CallState.Idle -> currentState.copy(
                            isConnected = false,
                            isConnecting = false,
                            error = null,
                            callStartTime = 0
                        )
                        else -> currentState
                    }
                }
            }
        }
    }

    fun startCall(user: User, isVideoCall: Boolean) {
        viewModelScope.launch {
            try {
                callManager.startCall(user, isVideoCall)
                analytics.trackEvent("call_view_started")
                logger.logEvent(
                    tag = TAG,
                    message = "Starting call with user: ${user.id}",
                    level = LogLevel.INFO
                )
            } catch (e: Exception) {
                handleError("Error starting call", e)
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            try {
                callManager.endCall()
                _currentFilter.value = null
                _showGameSelector.value = false

                val duration = calculateCallDuration()
                analytics.trackEvent("call_view_ended")
                logger.logEvent(
                    tag = TAG,
                    message = "Call ended. Duration: $duration ms",
                    level = LogLevel.INFO
                )
            } catch (e: Exception) {
                handleError("Error ending call", e)
            }
        }
    }

    fun toggleMute() {
        viewModelScope.launch {
            try {
                _isMuted.value = !_isMuted.value
                callManager.toggleMute(_isMuted.value)
                analytics.trackEvent(if (_isMuted.value) "call_muted" else "call_unmuted")
            } catch (e: Exception) {
                handleError("Error toggling mute", e)
            }
        }
    }

    fun toggleVideo() {
        viewModelScope.launch {
            try {
                _uiState.update { current -> current.copy(isVideoEnabled = !current.isVideoEnabled) }
                analytics.trackEvent(
                    if (_uiState.value.isVideoEnabled) "video_enabled" else "video_disabled"
                )
            } catch (e: Exception) {
                handleError("Error toggling video", e)
            }
        }
    }

    fun switchCamera() {
        viewModelScope.launch {
            try {
                analytics.trackEvent("camera_switched")
            } catch (e: Exception) {
                handleError("Error switching camera", e)
            }
        }
    }

    fun setFilter(filterType: FilterType) {
        viewModelScope.launch {
            try {
                val filter = FilterFactory.createFilter(filterType)
                _currentFilter.value = filter
                analytics.trackEvent("filter_applied")
            } catch (e: Exception) {
                handleError("Error applying filter", e)
            }
        }
    }

    fun showGameSelector() {
        _showGameSelector.value = true
    }

    fun hideGameSelector() {
        _showGameSelector.value = false
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleError(message: String, error: Exception) {
        viewModelScope.launch {
            logger.logEvent(
                tag = TAG,
                message = message,
                level = LogLevel.ERROR,
                throwable = error
            )
            _uiState.update { it.copy(
                error = "$message: ${error.message}"
            ) }
            analytics.trackEvent("call_view_error")
        }
    }

    private fun calculateCallDuration(): Long {
        val startTime = _uiState.value.callStartTime
        return if (startTime > 0) {
            System.currentTimeMillis() - startTime
        } else 0
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.isConnected) {
            endCall()
        }
    }

    fun toggleCamera() {
        viewModelScope.launch {
            try {
                _isCameraOn.value = !_isCameraOn.value
                analytics.trackEvent(
                    if (_isCameraOn.value) "camera_enabled" else "camera_disabled"
                )
            } catch (e: Exception) {
                handleError("Error toggling camera", e)
            }
        }
    }
}

data class CallUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val remoteUser: User? = null,
    val error: String? = null,
    val callStartTime: Long = 0
)