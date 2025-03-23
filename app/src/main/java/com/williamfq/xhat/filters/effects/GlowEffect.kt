/*
 * Updated: 2025-01-26
 * Author: William8677
 */

package com.williamfq.xhat.filters.effects

import android.graphics.Bitmap
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.base.Filter

/**
 * Implementación de un filtro "Glow" que extiende la clase base Filter.
 */
class GlowEffect : Filter() {

    /**
     * Aplica la lógica de "glow" sobre el Bitmap recibido.
     */
    override fun applyFilter(bitmap: Bitmap): Bitmap {
        return applyGlowLogic(bitmap)
    }

    /**
     * Indica que este filtro es de tipo GLOW.
     */
    override fun getType(): FilterType = FilterType.GLOW

    /**
     * Define los parámetros específicos del glow (intensidad, radio, etc.).
     * Si no necesitas parámetros adicionales, puedes devolver un mapa vacío.
     */
    override fun getParameters(): Map<String, Any> {
        return mapOf(
            "intensity" to 0.8f,
            "radius" to 15f
        )
    }

    /**
     * Lógica interna para aplicar un efecto "glow" (ejemplo).
     */
    private fun applyGlowLogic(bitmap: Bitmap): Bitmap {
        val glowBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        // Aquí implementarías la combinación de desenfoque + aclarado u otra técnica
        // Por ejemplo, hacer un blur e incrementar la luminosidad en las zonas brillantes
        return glowBitmap
    }
}
