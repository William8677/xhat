/*
 * Updated: 2025-01-26 17:40:23
 * Author: William8677
 */

package com.williamfq.xhat.call.audio

/**
 * Interfaz para el procesamiento de audio en llamadas.
 * Define las operaciones básicas para manejar el audio durante las llamadas.
 */
interface AudioProcessor {
    /**
     * Inicializa el procesador de audio.
     * @throws AudioProcessingException si hay un error en la inicialización
     */
    fun initialize()

    /**
     * Inicia el procesamiento de audio para una llamada.
     * @param callId Identificador único de la llamada (opcional)
     * @throws AudioProcessingException si hay un error al iniciar el procesamiento
     */
    fun startAudioProcessing(callId: String? = null)

    /**
     * Detiene el procesamiento de audio.
     * @throws AudioProcessingException si hay un error al detener el procesamiento
     */
    fun stopAudioProcessing()

    /**
     * Libera los recursos del procesador de audio.
     * @throws AudioProcessingException si hay un error al liberar los recursos
     */
    fun release()

    /**
     * Activa o desactiva el altavoz.
     * @param enabled true para activar el altavoz, false para desactivarlo
     */
    fun setSpeakerphoneEnabled(enabled: Boolean)

    /**
     * Ajusta el volumen de la llamada.
     * @param volume Nivel de volumen
     * @param showUI true para mostrar la UI de volumen
     */
    fun setCallVolume(volume: Int, showUI: Boolean = false)
    fun stopProcessing()
}

/**
 * Excepción personalizada para errores de procesamiento de audio.
 */
class AudioProcessingException(message: String, cause: Throwable? = null) :
    Exception(message, cause)