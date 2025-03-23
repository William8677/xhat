package com.williamfq.xhat.utils.filters

import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import org.webrtc.EglBase
import org.webrtc.VideoFrame.Buffer
import org.webrtc.VideoFrame.I420Buffer
import org.webrtc.VideoFrame.TextureBuffer
import org.webrtc.YuvConverter
import org.webrtc.TextureBufferImpl as WebRTCTextureBufferImpl
import org.webrtc.VideoFrameDrawer

class TextureBufferImpl(
    width: Int,
    height: Int,
    textureId: Int,
    context: EglBase.Context
) : TextureBuffer {


    private val transformMatrix = Matrix()


    private val videoFrameDrawer = VideoFrameDrawer()

    private val yuvConverter = YuvConverter(videoFrameDrawer)
    private val handler = Handler(Looper.getMainLooper())
    private val buffer: WebRTCTextureBufferImpl

    init {

        val cleanupRunnable: Runnable? = Runnable {
            try {
                yuvConverter.release()
            } catch (_: Exception) {

            }
        }


        buffer = WebRTCTextureBufferImpl(
            width,
            height,
            TextureBuffer.Type.OES,
            textureId,
            transformMatrix,
            handler,
            yuvConverter,
            cleanupRunnable
        )
    }

    override fun getWidth(): Int = buffer.width

    override fun getHeight(): Int = buffer.height

    override fun getType(): TextureBuffer.Type = buffer.type

    override fun getTextureId(): Int = buffer.textureId

    override fun getTransformMatrix(): Matrix = transformMatrix

    override fun toI420(): I420Buffer? = buffer.toI420()

    override fun cropAndScale(
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int,
        scaleWidth: Int,
        scaleHeight: Int
    ): Buffer = buffer.cropAndScale(cropX, cropY, cropWidth, cropHeight, scaleWidth, scaleHeight)

    override fun retain() = buffer.retain()

    override fun release() {
        buffer.release()
    }

    companion object {
        private const val TAG = "TextureBufferImpl"
    }
}
