
package com.xhat.core.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import androidx.core.graphics.scale

class FaceFilterManager {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * Aplica un filtro específico sobre cada rostro detectado en la imagen.
     *
     * @param originalBitmap La imagen original.
     * @param filterBitmap El bitmap del filtro a superponer (por ejemplo, una máscara).
     * @return La imagen con los filtros aplicados sobre los rostros.
     */
    fun applyFaceFilter(originalBitmap: Bitmap, filterBitmap: Bitmap, callback: (Bitmap) -> Unit) {
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()

        val image = InputImage.fromBitmap(originalBitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val boundingBox = face.boundingBox

                    // Ajusta el tamaño del filterBitmap según el tamaño del rostro detectado
                    val scaledFilter = filterBitmap.scale(boundingBox.width(), boundingBox.height())

                    canvas.drawBitmap(
                        scaledFilter,
                        boundingBox.left.toFloat(),
                        boundingBox.top.toFloat(),
                        paint
                    )
                }
                callback(mutableBitmap)
            }
            .addOnFailureListener { e ->
                Log.e("FaceFilterManager", "Face detection failed: ${e.message}")
                callback(originalBitmap) // Retorna la imagen original en caso de fallo
            }
    }
}
