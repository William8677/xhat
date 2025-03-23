package com.williamfq.xhat.camera.utils

import android.graphics.ImageFormat
import android.media.Image
import org.webrtc.VideoFrame
import java.nio.ByteBuffer

/**
 * Convierte un [Image] de la cámara en un [VideoFrame] para usar con WebRTC.
 */
object ImageConverter {
    fun convertImageToVideoFrame(image: Image): VideoFrame {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // En NV21, la disposición de bytes es: [Y bytes] + [V byte, U byte, V byte, U byte ...]
        val nv21Buffer = ByteBuffer.allocateDirect(ySize + uSize + vSize)

        // Copiar Y
        nv21Buffer.put(yBuffer)
        // Copiar U y V intercalados
        for (i in 0 until uSize) {
            nv21Buffer.put(vBuffer[i])
            nv21Buffer.put(uBuffer[i])
        }

        nv21Buffer.rewind()

        // OJO: VideoFrame(...) en WebRTC Java no admite named parameters
        return VideoFrame(
            /* buffer = */ createVideoFrameBuffer(nv21Buffer, image.width, image.height),
            /* rotation = */ 0,
            /* timestampNs = */ System.nanoTime()
        )
    }

    private fun createVideoFrameBuffer(
        buffer: ByteBuffer,
        width: Int,
        height: Int
    ): VideoFrame.Buffer {
        // Implementar la creación de un Buffer específico si quieres
        // manipularlo más adelante. Aquí nos limitamos a implementar
        // los métodos requeridos de la interfaz.
        return object : VideoFrame.Buffer {
            override fun getWidth(): Int = width
            override fun getHeight(): Int = height

            // Necesario para WebRTC
            override fun toI420(): VideoFrame.I420Buffer {
                // Si quieres convertir NV21 a I420, debes implementar la lógica.
                // Por ahora, lanzamos un error para indicar que no está hecho.
                throw NotImplementedError("toI420 conversion not implemented")
            }

            // WebRTC exige que implementemos cropAndScale
            override fun cropAndScale(
                x: Int,
                y: Int,
                cropWidth: Int,
                cropHeight: Int,
                scaleWidth: Int,
                scaleHeight: Int
            ): VideoFrame.Buffer {
                // Implementa la lógica para recortar y escalar.
                // Aquí, solo devolvemos un error de momento.
                throw NotImplementedError("cropAndScale not implemented")
            }

            override fun retain() {
                // En caso de necesitar conteo de referencias
            }

            override fun release() {
                // Liberar si es necesario
            }
        }
    }
}
