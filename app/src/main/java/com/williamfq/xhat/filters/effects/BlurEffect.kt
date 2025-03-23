/*
 * Updated: 2025-01-26
 * Author: William8677
 */

package com.williamfq.xhat.filters.effects

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import com.williamfq.xhat.R
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.base.Filter

/**
 * Implementación del efecto de desenfoque (Blur) que extiende la clase base Filter.
 */
class BlurEffect : Filter() {

    /**
     * Aplica el filtro de desenfoque al Bitmap proporcionado.
     */
    override fun applyFilter(bitmap: Bitmap): Bitmap {
        return applyBlurEffect(bitmap)
    }

    /**
     * Lógica interna para aplicar el efecto de desenfoque.
     */
    private fun applyBlurEffect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(blurredBitmap)
        val paint = Paint()

        // Configurar el efecto de desenfoque
        val blurRadius = (getParameters()["blurRadius"] as? Float) ?: 10f
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)

        // Dibujar la imagen con el efecto
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return blurredBitmap
    }

    /**
     * Retorna el tipo de filtro, en este caso, BLUR del enum FilterType.
     */
    override fun getType(): FilterType = FilterType.BLUR

    /**
     * Retorna los parámetros específicos para este filtro.
     */
    override fun getParameters(): Map<String, Any> = mapOf(
        "blurRadius" to 10f,
        "intensity" to 0.5f
    )
}
