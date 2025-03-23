package com.williamfq.xhat.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.williamfq.xhat.service.events.EventBus
import com.williamfq.xhat.service.events.SubtitleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

class RealTimeSubtitleService : LifecycleService() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "RealTimeSubtitleService"
        private var isRunning = false

        fun isServiceRunning() = isRunning
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        initializeSpeechRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!::speechRecognizer.isInitialized) {
            initializeSpeechRecognizer()
        }
        return START_STICKY
    }

    private fun initializeSpeechRecognizer() {
        try {
            if (::speechRecognizer.isInitialized) {
                speechRecognizer.destroy()
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(createRecognitionListener())
            }

            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            }

            startListening()
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error al inicializar el reconocimiento de voz: ${e.message}")
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Timber.tag(TAG).d("Listo para escuchar")
        }

        override fun onBeginningOfSpeech() {
            Timber.tag(TAG).d("Comenzando a escuchar")
        }

        override fun onResults(results: Bundle?) {
            handleSpeechResults(results, false)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            handleSpeechResults(partialResults, true)
        }

        override fun onEndOfSpeech() {
            Timber.tag(TAG).d("Fin de la entrada de voz")
            restartListening()
        }

        override fun onError(error: Int) {
            val errorMessage = getErrorMessage(error)
            Timber.tag(TAG).e("Error de reconocimiento: $errorMessage")
            restartListening()
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Implementar si necesitas manejar cambios en el nivel de audio
        }

        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleSpeechResults(results: Bundle?, isPartial: Boolean) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { resultList ->
            if (resultList.isNotEmpty()) {
                val detectedText = resultList[0]
                Timber.tag(TAG).d("${if (isPartial) "Parcial" else "Final"}: $detectedText")
                broadcastSubtitle(detectedText)
            }
        }
    }

    private fun broadcastSubtitle(subtitle: String) {
        serviceScope.launch {
            try {
                EventBus.emit(SubtitleEvent(subtitle))
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error al emitir subtítulo: ${e.message}")
            }
        }
    }

    private fun startListening() {
        try {
            speechRecognizer.startListening(recognizerIntent)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error al iniciar la escucha: ${e.message}")
        }
    }

    private fun restartListening() {
        try {
            speechRecognizer.stopListening()
            startListening()
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error al reiniciar la escucha: ${e.message}")
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> "Error del cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
            SpeechRecognizer.ERROR_NETWORK -> "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado"
            SpeechRecognizer.ERROR_NO_MATCH -> "No se encontraron coincidencias"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
            SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
            else -> "Error desconocido"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::speechRecognizer.isInitialized) {
            try {
                speechRecognizer.stopListening()
                speechRecognizer.destroy()
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error al destruir el reconocedor: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}