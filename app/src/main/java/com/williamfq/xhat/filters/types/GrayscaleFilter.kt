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

class GrayscaleFilter : Filter() {
    override fun applyFilter(bitmap: Bitmap): Bitmap {
        return applyGrayscaleEffect(bitmap)
    }

    private fun applyGrayscaleEffect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()

        // Configurar la matriz de color para escala de grises
        val intensity = getParameters()["intensity"] as Float
        val saturation = 1 - intensity // Invierte la intensidad para el control de saturación
        val colorMatrix = ColorMatrix().apply {
            setSaturation(saturation)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayscaleBitmap
    }

    override fun getType(): FilterType = FilterType.GRAYSCALE

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to 1.0f,  // 1.0 = completamente en escala de grises, 0.0 = original
        "contrast" to 1.0f    // Factor de contraste adicional
    )

    override fun getDescription(): String {
        return "Convierte la imagen a tonos de gris."
    }
}
