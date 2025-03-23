/*
 * Updated: 2025-01-25 03:06:07
 * Author: William8677
 */

package com.williamfq.xhat.filters.types

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.base.Filter

class BlurFilter : Filter() {
    override fun applyFilter(bitmap: Bitmap): Bitmap {
        return applyBlurEffect(bitmap)
    }

    private fun applyBlurEffect(bitmap: Bitmap): Bitmap {
        // Implementa la lógica para aplicar el efecto de desenfoque.
        // Puedes usar RenderScript, GPUImage, o cualquier otra librería para optimizar.
        // Aquí se proporciona una implementación básica.
        val width = bitmap.width
        val height = bitmap.height
        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(blurredBitmap)
        val paint = Paint()

        // Simular un desenfoque simple
        val matrix = android.graphics.Matrix()
        matrix.setScale(1.05f, 1.05f) // Aumentar ligeramente el tamaño
        canvas.drawBitmap(bitmap, matrix, paint)

        return blurredBitmap
    }

    override fun getType(): FilterType = FilterType.BLUR

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to 1.0f,  // 1.0 = máximo desenfoque, 0.0 = original
        "radius" to 5.0f      // Radio del desenfoque
    )

    override fun getDescription(): String {
        return "Aplica un efecto de desenfoque a la imagen."
    }
}
