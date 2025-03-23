package com.williamfq.xhat.call.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.williamfq.domain.model.User
import com.williamfq.xhat.call.CallManager
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {
    @Inject lateinit var callManager: CallManager
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var notificationManager: CallNotificationManager
    @Inject lateinit var logger: LoggerInterface

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_ACCEPT_CALL = "com.williamfq.xhat.action.ACCEPT_CALL"
        const val ACTION_REJECT_CALL = "com.williamfq.xhat.action.REJECT_CALL"
        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_AVATAR_URL = "extra_avatar_url"
        const val EXTRA_IS_VIDEO_CALL = "extra_is_video_call"
        private const val TAG = "CallActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return
        val username = intent.getStringExtra(EXTRA_USERNAME) ?: return
        val avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL) ?: return
        val isVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false)

        val user = User(
            id = userId,
            username = username,
            avatarUrl = avatarUrl,
            name = "Usuario $username"
        )

        receiverScope.launch {
            try {
                when (intent.action) {
                    ACTION_ACCEPT_CALL -> handleAcceptCall(user, isVideoCall, callId)
                    ACTION_REJECT_CALL -> handleRejectCall()
                }
            } catch (e: Exception) {
                logger.logEvent(TAG, "Error manejando acción de llamada: ${e.message}", LogLevel.ERROR, e)
            }
        }
    }

    private suspend fun handleAcceptCall(user: User, isVideoCall: Boolean, callId: String) {
        try {
            notificationManager.cancelIncomingCallNotification()
            callManager.startCall(user, isVideoCall)
            analytics.trackEvent("call_accepted")
            logger.logEvent(TAG, "Llamada aceptada: user=${user.username}, video=$isVideoCall", LogLevel.INFO)
        } catch (e: Exception) {
            logger.logEvent(TAG, "Error aceptando llamada", LogLevel.ERROR, e)
        }
    }

    private suspend fun handleRejectCall() {
        try {
            notificationManager.cancelIncomingCallNotification()
            callManager.endCall()
            analytics.trackEvent("call_rejected")
            logger.logEvent(TAG, "Llamada rechazada", LogLevel.INFO)
        } catch (e: Exception) {
            logger.logEvent(TAG, "Error rechazando llamada", LogLevel.ERROR, e)
        }
    }
}