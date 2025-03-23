/*
 * Updated: 2025-02-12 17:48:32
 * Author: William8677
 */

package com.williamfq.xhat.utils.ar

import android.content.Context
import android.graphics.*
import android.media.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceLandmark
import org.webrtc.VideoFrame
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.experimental.and

object ARUtils {
    fun calculateTransformationMatrix(angleX: Float, angleY: Float, angleZ: Float): Matrix {
        val matrix = Matrix()
        matrix.postRotate(angleZ)
        matrix.postRotate(angleY, 0f, 0f)
        matrix.postRotate(angleX, 0f, 0f)
        return matrix
    }

    fun calculateForeheadPosition(landmarks: List<FaceLandmark>): PointF {
        val leftEye = landmarks.find { it.landmarkType == FaceLandmark.LEFT_EYE }?.position
        val rightEye = landmarks.find { it.landmarkType == FaceLandmark.RIGHT_EYE }?.position

        return if (leftEye != null && rightEye != null) {
            val centerX = (leftEye.x + rightEye.x) / 2
            val centerY = leftEye.y - (rightEye.x - leftEye.x) * 0.5f
            PointF(centerX, centerY)
        } else {
            PointF(0f, 0f)
        }
    }

    fun drawARElement(
        videoFrame: VideoFrame,
        bitmap: Bitmap,
        position: PointF,
        matrix: Matrix,
        alpha: Float
    ) {
        val frameBitmap = frameToBitmap(videoFrame)
        val canvas = Canvas(frameBitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            this.alpha = (alpha * 255).toInt()
            isFilterBitmap = true
        }

        canvas.save()
        canvas.translate(position.x, position.y)
        canvas.concat(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.restore()
    }

    fun loadBitmap(context: Context, resourceId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resourceId)
    }

    fun convertFrameToInputImage(frame: VideoFrame): InputImage {
        val bitmap = frameToBitmap(frame)
        return InputImage.fromBitmap(bitmap, 0)
    }

    private fun frameToBitmap(frame: VideoFrame): Bitmap {
        val buffer = frame.buffer
        val width = buffer.width
        val height = buffer.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Convertir el buffer del frame a bitmap
        when (buffer) {
            is VideoFrame.TextureBuffer -> {
                // Implementar conversión de textura a bitmap
                val canvas = Canvas(bitmap)
                // ... lógica de conversión de textura
            }
            is VideoFrame.I420Buffer -> {
                // Convertir YUV a RGB
                val yuvData = ByteArray(width * height * 3 / 2)
                val yBuffer = buffer.dataY
                val uBuffer = buffer.dataU
                val vBuffer = buffer.dataV

                // Copiar datos YUV
                yBuffer.get(yuvData, 0, width * height)
                uBuffer.get(yuvData, width * height, width * height / 4)
                vBuffer.get(yuvData, width * height * 5 / 4, width * height / 4)

                // Convertir YUV a RGB
                val argb = IntArray(width * height)
                convertYUVToRGB(yuvData, argb, width, height)
                bitmap.setPixels(argb, 0, width, 0, 0, width, height)
            }
        }

        return bitmap
    }

    private fun convertYUVToRGB(
        yuv: ByteArray,
        argb: IntArray,
        width: Int,
        height: Int
    ) {
        val frameSize = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val yp = yuv[j * width + i] and 0xff.toByte()
                val uvp = yuv[frameSize + (j shr 1) * width + (i and -2)] and 0xff.toByte()
                val vp = yuv[frameSize + (j shr 1) * width + (i and -2) + 1] and 0xff.toByte()

                var r = yp + (1.402f * (vp - 128)).toInt()
                var g = yp - (0.344f * (uvp - 128)).toInt() - (0.714f * (vp - 128)).toInt()
                var b = yp + (1.772f * (uvp - 128)).toInt()

                r = if (r < 0) 0 else if (r > 255) 255 else r
                g = if (g < 0) 0 else if (g > 255) 255 else g
                b = if (b < 0) 0 else if (b > 255) 255 else b

                argb[j * width + i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            }
        }
    }
}