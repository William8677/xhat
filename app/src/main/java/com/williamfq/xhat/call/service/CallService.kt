/*
 * Updated: 2025-02-13 01:02:40
 * Author: William8677
 */

package com.williamfq.xhat.call.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.williamfq.domain.model.User
import com.williamfq.xhat.R
import com.williamfq.xhat.call.CallManager
import com.williamfq.xhat.call.audio.AudioProcessor
import com.williamfq.xhat.call.notifications.CallNotificationManager
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject lateinit var callManager: CallManager
    @Inject lateinit var notificationManager: CallNotificationManager
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var audioProcessor: AudioProcessor
    @Inject lateinit var logger: LoggerInterface

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var callDurationJob: Job? = null
    private var callStartTime: Long = 0

    companion object {
        private const val TAG = "CallService"
        private const val NOTIFICATION_ID = CallNotificationManager.NOTIFICATION_SERVICE_ID
        private const val ACTION_START_CALL = "com.williamfq.xhat.action.START_CALL"
        private const val ACTION_END_CALL = "com.williamfq.xhat.action.END_CALL"
        private const val EXTRA_USER = "extra_user"
        private const val EXTRA_IS_VIDEO_CALL = CallNotificationManager.EXTRA_IS_VIDEO_CALL
        private const val EXTRA_CALL_ID = CallNotificationManager.EXTRA_CALL_ID

        fun startCall(context: Context, user: User, isVideoCall: Boolean, callId: String) {
            try {
                val intent = Intent(context, CallService::class.java).apply {
                    action = ACTION_START_CALL
                    putExtra(EXTRA_USER, user as Parcelable)
                    putExtra(EXTRA_IS_VIDEO_CALL, isVideoCall)
                    putExtra(EXTRA_CALL_ID, callId)
                }
                context.startService(intent)
            } catch (e: Exception) {
                // No podemos usar logger aquí porque es un método estático
                e.printStackTrace()
            }
        }

        fun endCall(context: Context) {
            try {
                val intent = Intent(context, CallService::class.java).apply {
                    action = ACTION_END_CALL
                }
                context.startService(intent)
            } catch (e: Exception) {
                // No podemos usar logger aquí porque es un método estático
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            try {
                acquireWakeLock()
                setupAudio()
                logger.logEvent(TAG, "CallService onCreate completed", LogLevel.DEBUG)
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error in onCreate",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                analytics.trackEvent("call_service_error")
            }
        }
    }

    private fun setupAudio() {
        serviceScope.launch {
            try {
                audioProcessor.initialize()
                logger.logEvent(TAG, "Audio initialized successfully", LogLevel.DEBUG)
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error initializing audio",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                analytics.trackEvent("call_audio_error")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            logger.logEvent(TAG, "onStartCommand: ${intent?.action}", LogLevel.DEBUG)
            when (intent?.action) {
                ACTION_START_CALL -> handleStartCall(intent)
                ACTION_END_CALL -> handleEndCall()
            }
        }
        return START_STICKY
    }

    @Suppress("DEPRECATION")
    private suspend fun handleStartCall(intent: Intent) {
        try {
            val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_USER, User::class.java)
            } else {
                intent.getParcelableExtra(EXTRA_USER)
            }
            val isVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false)
            val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return

            logger.logEvent(
                tag = TAG,
                message = "Starting call: callId=$callId, isVideo=$isVideoCall",
                level = LogLevel.INFO
            )

            startForeground(
                NOTIFICATION_ID,
                createCallNotification(user, isVideoCall)
            )

            audioProcessor.startAudioProcessing()
            callStartTime = System.currentTimeMillis()
            startCallDurationTimer(user, isVideoCall)

            analytics.trackEvent("call_service_started")
        } catch (e: Exception) {
            logger.logEvent(
                tag = TAG,
                message = "Error starting call",
                level = LogLevel.ERROR,
                throwable = e
            )
            analytics.trackEvent("call_start_error")
            stopSelf()
        }
    }

    private suspend fun handleEndCall() {
        try {
            logger.logEvent(TAG, "Ending call", LogLevel.INFO)
            callDurationJob?.cancel()
            val duration = System.currentTimeMillis() - callStartTime

            audioProcessor.stopAudioProcessing()
            analytics.trackEvent("call_service_ended")

            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            logger.logEvent(
                tag = TAG,
                message = "Error ending call",
                level = LogLevel.ERROR,
                throwable = e
            )
            analytics.trackEvent("call_end_error")
            stopSelf()
        }
    }

    private fun startCallDurationTimer(user: User?, isVideoCall: Boolean) {
        callDurationJob?.cancel()
        callDurationJob = serviceScope.launch {
            try {
                while (isActive) {
                    val duration = System.currentTimeMillis() - callStartTime
                    val formattedDuration = formatDuration(duration)

                    user?.let {
                        notificationManager.showOngoingCallNotification(
                            user = it,
                            isVideoCall = isVideoCall,
                            callDuration = formattedDuration
                        )
                    }

                    delay(1000) // Actualizar cada segundo
                }
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error in duration timer",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                analytics.trackEvent("call_timer_error")
            }
        }
    }

    private fun createCallNotification(user: User?, isVideoCall: Boolean) =
        NotificationCompat.Builder(this, CallNotificationManager.CHANNEL_ONGOING_CALLS)
            .setSmallIcon(R.drawable.ic_call_ongoing)
            .setContentTitle(if (isVideoCall) "Videollamada en curso" else "Llamada en curso")
            .setContentText(user?.username ?: "Usuario desconocido")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_call_end,
                "Finalizar",
                createEndCallPendingIntent()
            )
            .build()

    private fun createEndCallPendingIntent() =
        android.app.PendingIntent.getService(
            this,
            0,
            Intent(this, CallService::class.java).apply { action = ACTION_END_CALL },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

    private suspend fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "XhatCall::CallServiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutos máximo
            }
            logger.logEvent(TAG, "WakeLock acquired", LogLevel.DEBUG)
        } catch (e: Exception) {
            logger.logEvent(
                tag = TAG,
                message = "Error acquiring WakeLock",
                level = LogLevel.ERROR,
                throwable = e
            )
            analytics.trackEvent("wake_lock_error")
        }
    }

    private fun formatDuration(duration: Long): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = duration / (1000 * 60 * 60)
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.launch {
            try {
                logger.logEvent(TAG, "onDestroy CallService", LogLevel.DEBUG)
                wakeLock?.release()
                serviceScope.cancel()
                audioProcessor.release()
                analytics.trackEvent("call_service_destroyed")
                super.onDestroy()
            } catch (e: Exception) {
                logger.logEvent(
                    tag = TAG,
                    message = "Error in onDestroy",
                    level = LogLevel.ERROR,
                    throwable = e
                )
                analytics.trackEvent("call_service_destroy_error")
                super.onDestroy()
            }
        }
    }
}