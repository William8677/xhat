/*
 * Updated: 2025-01-26 21:56:03
 * Author: William8677
 */

package com.williamfq.xhat.filters.types

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.base.Filter

class SepiaFilter : Filter() {
    override fun applyFilter(bitmap: Bitmap): Bitmap {
        return applySepiaEffect(bitmap)
    }

    private fun applySepiaEffect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sepiaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sepiaBitmap)
        val paint = Paint()

        // Configurar la matriz de color para efecto sepia
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)

            val scale = 1.2f
            setScale(scale, scale * 0.95f, scale * 0.82f, 1.0f)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return sepiaBitmap
    }

    override fun getType(): FilterType = FilterType.SEPIA

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to 1.0f,  // 1.0 = completamente sepia, 0.0 = original
        "contrast" to 1.0f    // Factor de contraste adicional
    )

    override fun getDescription(): String {
        return "Convierte la imagen a tonos de sepia."
    }
}
