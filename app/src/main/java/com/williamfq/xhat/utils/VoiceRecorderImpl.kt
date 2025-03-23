package com.williamfq.xhat.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Implementación concreta de VoiceRecorder utilizando MediaRecorder.
 *
 * Nota:
 * - Para API < 24, pause/resume no están disponibles, por lo que se notificará que no son soportados.
 * - La grabación se guarda en un archivo temporal en el directorio cache.
 */
class VoiceRecorderImpl(private val context: Context) : VoiceRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null
    private var isRecording: Boolean = false

    override fun start() {
        if (isRecording) {
            Timber.tag(TAG).w("Ya se está grabando")
            return
        }
        outputFile = File(context.cacheDir, "voice_recording_${System.currentTimeMillis()}.3gp").absolutePath
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
                isRecording = true
                Timber.tag(TAG).d("Grabación iniciada. Archivo: $outputFile")
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "Error al iniciar la grabación")
            } catch (e: IllegalStateException) {
                Timber.tag(TAG).e(e, "Estado ilegal al iniciar la grabación")
            }
        }
    }

    override fun stop() {
        if (!isRecording) {
            Timber.tag(TAG).w("No se está grabando, no se puede detener")
            return
        }
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            Timber.tag(TAG).d("Grabación detenida. Archivo guardado: $outputFile")
        } catch (e: RuntimeException) {
            Timber.tag(TAG).e(e, "Error al detener la grabación")
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }

    override fun pause() {
        if (!isRecording) {
            Timber.tag(TAG).w("No se está grabando, no se puede pausar")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                Timber.tag(TAG).d("Grabación en pausa")
            } catch (e: RuntimeException) {
                Timber.tag(TAG).e(e, "Error al pausar la grabación")
            }
        } else {
            Timber.tag(TAG).w("Pause no soportado en esta versión de Android")
        }
    }

    override fun resume() {
        if (!isRecording) {
            Timber.tag(TAG).w("No se está grabando, no se puede reanudar")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                Timber.tag(TAG).d("Grabación reanudada")
            } catch (e: RuntimeException) {
                Timber.tag(TAG).e(e, "Error al reanudar la grabación")
            }
        } else {
            Timber.tag(TAG).w("Resume no soportado en esta versión de Android")
        }
    }

    companion object {
        private const val TAG = "VoiceRecorderImpl"
    }
}
