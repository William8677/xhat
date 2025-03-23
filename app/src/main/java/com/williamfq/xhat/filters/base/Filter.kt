package com.williamfq.xhat.filters.base

import android.graphics.Bitmap
import com.williamfq.xhat.domain.model.FilterType
import org.webrtc.SurfaceViewRenderer

/**
 * Clase base para todos los filtros.
 * Define los métodos abstractos que cada filtro debe implementar.
 */
abstract class Filter {
    /**
     * Aplica el filtro a un Bitmap y retorna el Bitmap resultante.
     */
    abstract fun applyFilter(bitmap: Bitmap): Bitmap

    /**
     * Retorna el tipo de filtro (definido en FilterType).
     */
    abstract fun getType(): FilterType

    /**
     * Retorna un mapa de parámetros para este filtro.
     */
    abstract fun getParameters(): Map<String, Any>

    /**
     * Retorna el nombre del filtro (por defecto, el nombre del enum).
     */
    open fun getName(): String = getType().name

    /**
     * Retorna la descripción del filtro (por defecto, la descripción del enum).
     */
    open fun getDescription(): String = getType().description

    /**
     * Aplica el filtro a un SurfaceViewRenderer.
     */
    open fun applyToRenderer(renderer: SurfaceViewRenderer) {
        // Lógica para aplicar el filtro al renderer
    }
}