package com.williamfq.xhat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.williamfq.xhat.R
import com.williamfq.xhat.service.signaling.WebRTCClient
import com.williamfq.domain.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoTrack
import org.webrtc.AudioTrack
import org.webrtc.audio.JavaAudioDeviceModule
import javax.inject.Inject

@AndroidEntryPoint
class WalkieTalkieService : Service(), WebRTCClient.WebRTCListener {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "WalkieTalkieChannel"
        private const val SAMPLE_RATE = 44100 // Alta calidad de audio
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private val binder = WalkieTalkieBinder()
    private var audioRecord: AudioRecord? = null
    private var audioTrack: android.media.AudioTrack? = null
    private var isTransmitting = false
    private var currentChatId: String? = null
    private lateinit var serviceScope: CoroutineScope
    private var mediaPlayer: MediaPlayer? = null
    private var webRTCClient: WebRTCClient? = null

    private val _walkieTalkieState = MutableStateFlow<WalkieTalkieState>(WalkieTalkieState.Idle)
    val walkieTalkieState: StateFlow<WalkieTalkieState> = _walkieTalkieState

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var chatRepository: ChatRepository // Repositorio para manejar la comunicación

    inner class WalkieTalkieBinder : Binder() {
        fun getService(): WalkieTalkieService = this@WalkieTalkieService
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.Default + Job())
        createNotificationChannel()
        initializeAudioEffects()
        initializeWebRTC()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Walkie Talkie",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para la función Walkie Talkie en chats"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun initializeAudioEffects() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build()
            )
        }
    }

    private fun initializeWebRTC() {
        val options = PeerConnectionFactory.InitializationOptions.builder(this)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val audioDeviceModule = JavaAudioDeviceModule.builder(this).createAudioDeviceModule()
        val factory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        webRTCClient = WebRTCClient(
            context = this,
            eglBaseContext = EglBase.create().eglBaseContext,
            listener = this
        )
    }

    fun startWalkieTalkie(chatId: String) {
        if (!isTransmitting && currentChatId != chatId) {
            currentChatId = chatId
            serviceScope.launch {
                try {
                    playStartSound()
                    _walkieTalkieState.value = WalkieTalkieState.Starting

                    // Notificar al otro usuario/grupo que se iniciará transmisión
                    chatRepository.notifyWalkieTalkieStart(chatId)

                    if (checkPermissions()) {
                        setupAudioRecording()
                        isTransmitting = true
                        _walkieTalkieState.value = WalkieTalkieState.Transmitting

                        // Comenzar a transmitir audio
                        startAudioTransmission(chatId)

                        // Comenzar a recibir audio
                        startReceivingAudio(chatId)
                    } else {
                        handleError(SecurityException("Faltan permisos para grabar audio"))
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    fun stopWalkieTalkie() {
        if (isTransmitting) {
            serviceScope.launch {
                try {
                    playEndSound()
                    isTransmitting = false

                    // Notificar que se detuvo la transmisión
                    currentChatId?.let { chatId ->
                        chatRepository.notifyWalkieTalkieStop(chatId)
                    }

                    cleanupResources()
                    _walkieTalkieState.value = WalkieTalkieState.Idle
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        val modifyAudioSettingsPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
        return recordAudioPermission == PackageManager.PERMISSION_GRANTED && modifyAudioSettingsPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun setupAudioRecording() {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            audioTrack = android.media.AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()
        } catch (e: SecurityException) {
            handleError(e)
        }
    }

    private suspend fun startAudioTransmission(chatId: String) {
        withContext(Dispatchers.IO) {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
            val buffer = ByteArray(bufferSize)

            try {
                audioRecord?.startRecording()

                while (isTransmitting) {
                    val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (bytesRead > 0) {
                        // Enviar el audio a través del repositorio
                        chatRepository.sendWalkieTalkieAudio(chatId, buffer.copyOf(bytesRead))
                    }
                }

                audioRecord?.stop()
            } catch (e: SecurityException) {
                handleError(e)
            }
        }
    }

    private suspend fun startReceivingAudio(chatId: String) {
        withContext(Dispatchers.IO) {
            chatRepository.receiveWalkieTalkieAudio(chatId).collect { audioData ->
                try {
                    audioTrack?.write(audioData, 0, audioData.size)
                } catch (e: SecurityException) {
                    handleError(e)
                }
            }
        }
    }

    private fun playStartSound() {
        mediaPlayer?.apply {
            reset()
            resources.openRawResourceFd(R.raw.walkie_start)?.use {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
                prepare()
                start()
            }
        }
    }

    private fun playEndSound() {
        mediaPlayer?.apply {
            reset()
            resources.openRawResourceFd(R.raw.walkie_end)?.use {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
                prepare()
                start()
            }
        }
    }

    private fun cleanupResources() {
        audioRecord?.apply {
            stop()
            release()
        }
        audioTrack?.apply {
            stop()
            release()
        }
        audioRecord = null
        audioTrack = null
        currentChatId = null
    }

    private fun handleError(error: Exception) {
        _walkieTalkieState.value = WalkieTalkieState.Error(error.message ?: "Error desconocido")
        cleanupResources()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaPlayer?.release()
        cleanupResources()
    }

    // Implementación de WebRTCListener
    override fun onCallConnected() {
        // Notificar que se ha conectado la llamada
    }

    override fun onCallEnded() {
        stopWalkieTalkie()
    }

    override fun onLocalVideoTrackCreated(videoTrack: VideoTrack) {
        // Configurar el video track local
    }

    override fun onRemoteVideoTrackReceived(videoTrack: VideoTrack) {
        // Configurar el video track remoto
    }

    override fun onLocalAudioTrackCreated(audioTrack: AudioTrack) {
        // Configurar el audio track local
    }

    override fun onRemoteAudioTrackReceived(audioTrack: AudioTrack) {
        // Configurar el audio track remoto
    }

    override fun onError(error: String) {
        handleError(Exception(error))
    }
}

sealed class WalkieTalkieState {
    object Idle : WalkieTalkieState()
    object Starting : WalkieTalkieState()
    object Transmitting : WalkieTalkieState()
    data class Error(val message: String) : WalkieTalkieState()
}