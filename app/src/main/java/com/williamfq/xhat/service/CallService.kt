package com.williamfq.xhat.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.williamfq.xhat.R
import com.williamfq.xhat.call.audio.AudioProcessor
import com.williamfq.xhat.call.model.*
import com.williamfq.xhat.utils.audio.SoundManager
import com.williamfq.domain.model.User // Ensure correct import for User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CallService : LifecycleService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binder = LocalBinder()
    private var wakeLock: PowerManager.WakeLock? = null
    private var isIncomingCall = false

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var audioProcessor: AudioProcessor

    @Inject
    lateinit var soundManager: SoundManager

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val CHANNEL_ID = "CallServiceChannel"
        private const val TAG = "CallService"
    }

    inner class LocalBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    override fun onCreate() {
        super.onCreate()
        initializeService()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun initializeService() {
        createNotificationChannel()
        initializeAudio()
        initializeWakeLock()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Llamadas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para llamadas de voz y video"
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
            }

            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun initializeAudio() {
        audioProcessor.initialize()
    }

    private fun initializeWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "XhatCall::CallWakeLock"
        )
    }

    fun startCall(remoteUser: User, isVideo: Boolean = false) {
        serviceScope.launch {
            try {
                wakeLock?.acquire(30 * 60 * 1000L) // 30 minutos máximo
                _callState.value = CallState.Connecting(remoteUser, isVideo)

                startForeground(NOTIFICATION_ID, createCallNotification())
                configureAudioForCall(isVideo)

                if (isVideo) {
                    startVideoService()
                }

                _callState.value = CallState.Connected(remoteUser, isVideo)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error starting call")
                _callState.value = CallState.Error(CallErrorCode.GENERIC_ERROR, e.message ?: "Error al iniciar llamada")
                endCall()
            }
        }
    }

    fun handleIncomingCall(remoteUser: User, callType: CallType) {
        serviceScope.launch {
            isIncomingCall = true
            _callState.value = CallState.Ringing(remoteUser, callType, isIncoming = true)
            startRingtone()
            startForeground(NOTIFICATION_ID, createIncomingCallNotification(remoteUser.username))
        }
    }

    fun acceptCall() {
        serviceScope.launch {
            stopRingtone()
            startCall((_callState.value as? CallState.Ringing)?.remoteUser ?: return@launch, (_callState.value as? CallState.Ringing)?.callType == CallType.VIDEO)
        }
    }

    fun rejectCall() {
        serviceScope.launch {
            stopRingtone()
            endCall()
        }
    }

    fun endCall() {
        serviceScope.launch {
            try {
                stopRingtone()
                audioProcessor.stopProcessing()

                wakeLock?.release()
                _callState.value = CallState.Ended(CallEndReason.HANGUP, System.currentTimeMillis())

                stopForeground(true)
                stopSelf()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error ending call")
            }
        }
    }

    fun toggleMute() {
        audioManager.isMicrophoneMute = !audioManager.isMicrophoneMute
    }

    fun toggleSpeaker() {
        audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
    }

    private fun startRingtone() {
        soundManager.playCallRingtone()
    }

    private fun stopRingtone() {
        soundManager.stopCallRingtone()
    }

    private fun configureAudioForCall(isVideo: Boolean) {
        audioProcessor.startAudioProcessing(callState.value.toString())
        audioManager.isSpeakerphoneOn = isVideo
        audioManager.isMicrophoneMute = false
    }

    private fun startVideoService() {
        val videoIntent = Intent(this, VideoCallService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(videoIntent)
        } else {
            startService(videoIntent)
        }
    }

    private fun createCallNotification(): Notification {
        val intent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada en curso")
            .setContentText("Toca para volver a la llamada")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_call_end,
                "Colgar",
                createEndCallPendingIntent()
            )
            .build()
    }

    private fun createIncomingCallNotification(callerName: String): Notification {
        val answerIntent = Intent(this, CallService::class.java).apply {
            action = "ANSWER_CALL"
        }
        val rejectIntent = Intent(this, CallService::class.java).apply {
            action = "REJECT_CALL"
        }

        val answerPendingIntent = PendingIntent.getService(
            this,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectPendingIntent = PendingIntent.getService(
            this,
            2,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada entrante")
            .setContentText("de $callerName")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_call_accept,
                "Contestar",
                answerPendingIntent
            )
            .addAction(
                R.drawable.ic_call_reject,
                "Rechazar",
                rejectPendingIntent
            )
            .build()
    }

    private fun createEndCallPendingIntent(): PendingIntent {
        val intent = Intent(this, CallService::class.java).apply {
            action = "END_CALL"
        }
        return PendingIntent.getService(
            this,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        audioProcessor.release()
        wakeLock?.release()
    }
}