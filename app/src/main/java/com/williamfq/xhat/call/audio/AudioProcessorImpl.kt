package com.williamfq.xhat.call.audio

import android.media.AudioManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del procesador de audio para llamadas.
 *
 * Maneja la configuración del audio durante las llamadas, incluyendo:
 * - Modo de audio
 * - Control del altavoz
 * - Volumen de llamada
 * - Configuración de bluetooth
 *
 * Ejemplo de uso:
 * ```
 * audioProcessor.initialize()
 * audioProcessor.startAudioProcessing(callId)
 * // Durante la llamada...
 * audioProcessor.stopAudioProcessing()
 * audioProcessor.stopProcessing()
 * audioProcessor.release()
 * ```
 */
@Singleton
class AudioProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioManager: AudioManager
) : AudioProcessor {
    private var isInitialized = false
    private var currentCallId: String? = null

    override fun initialize() {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false
            isInitialized = true
        } catch (e: Exception) {
            throw AudioProcessingException("Error inicializando el procesador de audio", e)
        }
    }

    override fun startAudioProcessing(callId: String?) {
        if (!isInitialized) {
            initialize()
        }

        try {
            currentCallId = callId
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false

            // Configurar volumen inicial
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                currentVolume,
                0
            )
        } catch (e: Exception) {
            throw AudioProcessingException("Error iniciando el procesamiento de audio", e)
        }
    }

    override fun stopAudioProcessing() {
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            currentCallId = null
        } catch (e: Exception) {
            throw AudioProcessingException("Error deteniendo el procesamiento de audio", e)
        }
    }

    // Implementación del método stopProcessing
    override fun stopProcessing() {
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            currentCallId = null
        } catch (e: Exception) {
            throw AudioProcessingException("Error deteniendo el procesamiento de audio", e)
        }
    }

    override fun release() {
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            isInitialized = false
            currentCallId = null
        } catch (e: Exception) {
            throw AudioProcessingException("Error liberando el procesador de audio", e)
        }
    }

    override fun setSpeakerphoneEnabled(enabled: Boolean) {
        audioManager.isSpeakerphoneOn = enabled
    }

    override fun setCallVolume(volume: Int, showUI: Boolean) {
        val flags = if (showUI) AudioManager.FLAG_SHOW_UI else 0
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            volume.coerceIn(0, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)),
            flags
        )
    }
}