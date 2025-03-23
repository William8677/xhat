package com.williamfq.xhat.service

import android.app.*
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.williamfq.domain.model.User
import com.williamfq.xhat.R
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.call.model.CallErrorCode
import com.williamfq.xhat.call.model.CallEndReason
import com.williamfq.xhat.domain.model.FilterState
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.service.signaling.WebRTCClient
import com.williamfq.xhat.service.signaling.WebRTCSignalingClient
import com.williamfq.xhat.utils.filters.VideoFilterProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class VideoCallService : LifecycleService(), WebRTCClient.WebRTCListener {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // WebRTC components
    private var webRTCClient: WebRTCClient? = null
    private var eglBase: EglBase? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var remoteAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null

    // Estados
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    // Inyección de dependencias
    @Inject lateinit var audioManager: AudioManager
    @Inject lateinit var cameraManager: CameraManager
    @Inject lateinit var webRTCSignalingClient: WebRTCSignalingClient
    @Inject lateinit var filterProcessor: VideoFilterProcessor

    companion object {
        private const val NOTIFICATION_ID = 200
        private const val CHANNEL_ID = "VideoCallChannel"
        private const val TAG = "VideoCallService"

        private const val VIDEO_RESOLUTION_WIDTH = 1280
        private const val VIDEO_RESOLUTION_HEIGHT = 720
        private const val VIDEO_FPS = 30
    }

    inner class LocalBinder : Binder() {
        fun getService(): VideoCallService = this@VideoCallService
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
        initializeWebRTC()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Videollamadas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para videollamadas"
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun initializeWebRTC() {
        try {
            eglBase = EglBase.create()
            val eglBaseContext = eglBase?.eglBaseContext

            webRTCClient = WebRTCClient(
                context = this,
                eglBaseContext = eglBaseContext!!,
                listener = this
            ).apply {
                initialize(
                    videoWidth = VIDEO_RESOLUTION_WIDTH,
                    videoHeight = VIDEO_RESOLUTION_HEIGHT,
                    videoFps = VIDEO_FPS,
                    audioEnabled = true,
                    videoEnabled = true
                )
            }

            // Inicializar el procesador de filtros con el contexto EGL
            filterProcessor.initialize(eglBaseContext!!)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error initializing WebRTC")
            _callState.value = CallState.Error(
                code = CallErrorCode.GENERIC_ERROR,
                message = "Error al inicializar WebRTC"
            )
        }
    }

    // Define getUserById for demonstration purposes
    private fun getUserById(userId: String): User {
        // Retrieve user from your data source
        return User(userId, "Remote User", "https://example.com/avatar.jpg") // Add avatarUrl if necessary
    }

    fun startVideoCall(targetUserId: String) {
        serviceScope.launch {
            try {
                _callState.value = CallState.Connecting(
                    remoteUser = getUserById(targetUserId),
                    isVideoCall = true
                )
                startForeground(NOTIFICATION_ID, createVideoCallNotification())

                // Configurar audio
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.isSpeakerphoneOn = true

                // Iniciar WebRTC
                webRTCClient?.apply {
                    startLocalVideo()
                    createOffer(targetUserId)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error starting video call")
                _callState.value = CallState.Error(
                    code = CallErrorCode.GENERIC_ERROR,
                    message = e.message ?: "Error al iniciar videollamada"
                )
                endCall()
            }
        }
    }

    fun applyFilter(filter: Filter) {
        serviceScope.launch {
            try {
                _filterState.value = _filterState.value.copy(
                    currentFilter = filter.getType(),
                    isEnabled = true
                )

                // Aplicar filtro al video local
                localVideoTrack?.let { track ->
                    filterProcessor.setFilter(filter)
                    // Insertar el procesador de filtros en la cadena de video
                    track.addSink(filterProcessor as VideoSink)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error applying filter")
            }
        }
    }

    fun removeFilter() {
        serviceScope.launch {
            _filterState.value = _filterState.value.copy(
                currentFilter = null,
                isEnabled = false
            )

            // Remover filtro del video local
            localVideoTrack?.removeSink(filterProcessor as VideoSink)
        }
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun toggleVideo(enabled: Boolean) {
        localVideoTrack?.setEnabled(enabled)
    }

    fun toggleAudio(enabled: Boolean) {
        webRTCClient?.toggleAudio(enabled)
    }

    fun endCall() {
        serviceScope.launch {
            try {
                // Limpiar WebRTC
                webRTCClient?.apply {
                    stopLocalVideo()
                    disconnect()
                }

                // Limpiar filtros
                filterProcessor.release()

                // Restaurar audio
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.isSpeakerphoneOn = false

                // Actualizar estado y detener servicio
                val duration = System.currentTimeMillis() - ((_callState.value as? CallState.Connected)?.startTime ?: 0L)
                _callState.value = CallState.Ended(
                    reason = CallEndReason.UNKNOWN,
                    duration = duration
                )
                stopForeground(true)
                stopSelf()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error ending call")
            }
        }
    }

    // WebRTCListener implementations
    override fun onCallConnected() {
        _callState.value = CallState.Connected(
            remoteUser = getUserById(webRTCClient?.getRemoteUserId() ?: return),
            isVideoCall = true
        )
    }

    override fun onCallEnded() {
        endCall()
    }

    override fun onLocalVideoTrackCreated(videoTrack: VideoTrack) {
        localVideoTrack = videoTrack
    }

    override fun onRemoteVideoTrackReceived(videoTrack: VideoTrack) {
        remoteVideoTrack = videoTrack
    }

    override fun onLocalAudioTrackCreated(audioTrack: AudioTrack) {
        localAudioTrack = audioTrack
    }

    override fun onRemoteAudioTrackReceived(audioTrack: AudioTrack) {
        remoteAudioTrack = audioTrack
    }

    override fun onError(error: String) {
        _callState.value = CallState.Error(
            code = CallErrorCode.GENERIC_ERROR,
            message = error
        )
    }

    private fun createVideoCallNotification(): Notification {
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
            .setContentTitle("Videollamada en curso")
            .setContentText("Toca para volver a la videollamada")
            .setSmallIcon(R.drawable.ic_videocam)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_call_end,
                "Finalizar",
                createEndCallPendingIntent()
            )
            .build()
    }

    private fun createEndCallPendingIntent(): PendingIntent {
        val intent = Intent(this, VideoCallService::class.java).apply {
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
        serviceScope.cancel()
        webRTCClient?.release()
        filterProcessor.release()
        eglBase?.release()

    }
}