/*
 * Updated: 2025-02-12 17:51:03
 * Author: William8677
 */

package com.williamfq.xhat.utils.ar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.williamfq.xhat.R
import com.williamfq.xhat.utils.ar.ARUtils.convertFrameToInputImage
import com.williamfq.xhat.utils.ar.ARUtils.drawARElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.webrtc.VideoFrame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ARSystem @Inject constructor(
    private val context: Context
) {
    private var arAssets = mutableMapOf<String, ARAsset>()
    private var currentAsset by mutableStateOf<String?>(null)

    // Detector de rostros de ML Kit
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
    )

    init {
        loadARAssets()
    }

    private fun loadARAssets() {
        // Cargar assets para filtros AR
        arAssets = mutableMapOf(
            "dog_ears" to ARAsset(
                id = "dog_ears",
                name = "Orejas de Perro",
                bitmap = loadBitmap(R.drawable.dog_ears),
                attachmentPoints = listOf(
                    AttachmentPoint.LEFT_EAR,
                    AttachmentPoint.RIGHT_EAR
                ),
                scale = 1.2f,
                offsetY = -0.2f
            ),
            "flower_crown" to ARAsset(
                id = "flower_crown",
                name = "Corona de Flores",
                bitmap = loadBitmap(R.drawable.flower_crown),
                attachmentPoints = listOf(AttachmentPoint.FOREHEAD),
                scale = 1.5f,
                offsetY = -0.3f
            ),
            "cat_whiskers" to ARAsset(
                id = "cat_whiskers",
                name = "Bigotes de Gato",
                bitmap = loadBitmap(R.drawable.cat_whiskers),
                attachmentPoints = listOf(
                    AttachmentPoint.NOSE,
                    AttachmentPoint.LEFT_CHEEK,
                    AttachmentPoint.RIGHT_CHEEK
                ),
                scale = 1.0f
            ),
            "bunny_nose" to ARAsset(
                id = "bunny_nose",
                name = "Nariz de Conejo",
                bitmap = loadBitmap(R.drawable.bunny_nose),
                attachmentPoints = listOf(AttachmentPoint.NOSE),
                scale = 0.8f
            ),
            "angel_halo" to ARAsset(
                id = "angel_halo",
                name = "Aureola de Ángel",
                bitmap = loadBitmap(R.drawable.angel_halo),
                attachmentPoints = listOf(AttachmentPoint.HEAD_TOP),
                scale = 1.3f,
                offsetY = -0.4f
            ),
            "butterfly_crown" to ARAsset(
                id = "butterfly_crown",
                name = "Corona de Mariposas",
                bitmap = loadBitmap(R.drawable.butterfly_crown),
                attachmentPoints = listOf(AttachmentPoint.FOREHEAD),
                scale = 1.4f,
                offsetY = -0.25f,
                animated = true,
                animationFrames = 8,
                animationSpeed = 0.1f
            )
        )
    }

    suspend fun processFrame(videoFrame: VideoFrame): VideoFrame = withContext(Dispatchers.Default) {
        val currentARAsset = currentAsset?.let { arAssets[it] } ?: return@withContext videoFrame

        try {
            // Convertir frame a InputImage para ML Kit
            val image = convertFrameToInputImage(videoFrame)

            // Detectar rostros
            val faces = Tasks.await(faceDetector.process(image))

            // Procesar cada rostro detectado
            faces.forEach { face ->
                applyAREffectToFace(videoFrame, face, currentARAsset)
            }

            videoFrame
        } catch (e: Exception) {
            videoFrame
        }
    }

    private fun applyAREffectToFace(
        videoFrame: VideoFrame,
        face: Face,
        arAsset: ARAsset
    ) {
        // Matriz de transformación para el efecto AR
        val matrix = Matrix().apply {
            // Rotación basada en la pose de la cara
            postRotate(face.headEulerAngleZ)
            postRotate(face.headEulerAngleY, 0f, 0f)
            postRotate(face.headEulerAngleX, 0f, 0f)

            // Escala basada en el tamaño de la cara
            val faceWidth = face.boundingBox.width()
            val scale = faceWidth * arAsset.scale
            postScale(scale, scale)
        }

        // Aplicar el efecto en cada punto de anclaje
        arAsset.attachmentPoints.forEach { attachmentPoint ->
            val position = when (attachmentPoint) {
                AttachmentPoint.LEFT_EAR -> face.getLandmark(FaceLandmark.LEFT_EAR)?.position
                AttachmentPoint.RIGHT_EAR -> face.getLandmark(FaceLandmark.RIGHT_EAR)?.position
                AttachmentPoint.NOSE -> face.getLandmark(FaceLandmark.NOSE_BASE)?.position
                AttachmentPoint.FOREHEAD -> calculateForeheadPosition(face)
                AttachmentPoint.HEAD_TOP -> calculateHeadTopPosition(face)
                AttachmentPoint.LEFT_CHEEK -> face.getLandmark(FaceLandmark.LEFT_CHEEK)?.position
                AttachmentPoint.RIGHT_CHEEK -> face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.position
            }

            position?.let { pos ->
                // Ajustar posición con offset
                val adjustedPos = PointF(
                    pos.x,
                    pos.y + (arAsset.offsetY * face.boundingBox.height())
                )

                // Dibujar el elemento AR
                drawARElement(
                    videoFrame = videoFrame,
                    bitmap = arAsset.bitmap,
                    position = adjustedPos,
                    matrix = matrix,
                    alpha = arAsset.alpha
                )

                // Animar si es necesario
                if (arAsset.animated) {
                    updateAnimation(arAsset)
                }
            }
        }
    }

    private fun calculateForeheadPosition(face: Face): PointF {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position

        return if (leftEye != null && rightEye != null) {
            val centerX = (leftEye.x + rightEye.x) / 2
            val centerY = leftEye.y - (rightEye.x - leftEye.x) * 0.5f
            PointF(centerX, centerY)
        } else {
            PointF(
                face.boundingBox.centerX().toFloat(),
                face.boundingBox.top.toFloat()
            )
        }
    }

    private fun calculateHeadTopPosition(face: Face): PointF {
        return PointF(
            face.boundingBox.centerX().toFloat(),
            face.boundingBox.top.toFloat() - (face.boundingBox.height() * 0.2f)
        )
    }

    private fun updateAnimation(arAsset: ARAsset) {
        if (System.currentTimeMillis() - arAsset.lastAnimationUpdate > (arAsset.animationSpeed * 1000)) {
            arAsset.currentFrame = (arAsset.currentFrame + 1) % arAsset.animationFrames
            arAsset.lastAnimationUpdate = System.currentTimeMillis()
        }
    }

    fun setARAsset(assetId: String?) {
        currentAsset = assetId
    }

    fun getAvailableAssets(): List<ARAsset> = arAssets.values.toList()

    private fun loadBitmap(resourceId: Int): Bitmap {
        return context.resources.openRawResource(resourceId).use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    data class ARAsset(
        val id: String,
        val name: String,
        val bitmap: Bitmap,
        val attachmentPoints: List<AttachmentPoint>,
        val scale: Float = 1.0f,
        val offsetY: Float = 0f,
        val alpha: Float = 1f,
        val animated: Boolean = false,
        val animationFrames: Int = 1,
        val animationSpeed: Float = 0.1f,
        var currentFrame: Int = 0,
        var lastAnimationUpdate: Long = 0L
    )

    enum class AttachmentPoint {
        LEFT_EAR,
        RIGHT_EAR,
        NOSE,
        FOREHEAD,
        HEAD_TOP,
        LEFT_CHEEK,
        RIGHT_CHEEK
    }
}