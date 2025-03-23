/*
 * Updated: 2025-01-27 23:30:00
 * Author: William8677
 */

package com.williamfq.xhat.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Un EventBus basado en coroutines para emitir y observar eventos de la app.
 */
@Singleton
class EventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AppEvent>()
    val events = _events.asSharedFlow()

    /**
     * Emite un evento tipado.
     */
    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }

    /**
     * “Postea” un evento genérico usando un string, si lo prefieres.
     */
    suspend fun post(eventName: String) {
        _events.emit(AppEvent.GenericEvent(eventName))
    }
}

/**
 * Eventos de la app, usando una sealed class para mayor robustez.
 */
sealed class AppEvent {

    /**
     * Evento genérico si no quieres definir uno específico.
     */
    data class GenericEvent(val eventName: String) : AppEvent()

    // Ejemplos de eventos concretos que puedes usar en tu app:

    object CallStarted : AppEvent()
    object CallEnded : AppEvent()

    data class CameraError(val message: String) : AppEvent()

    data class ProcessingStarted(val imageId: String, val filterName: String) : AppEvent()
    data class ProcessingProgress(val imageId: String, val progress: Int, val total: Int) : AppEvent()
    data class ProcessingCompleted(val imageId: String, val outputUri: String) : AppEvent()
    data class ProcessingError(val imageId: String, val error: String) : AppEvent()

    object CacheCleared : AppEvent()
    // ...agrega más subclases según tu dominio
}
