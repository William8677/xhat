package com.williamfq.xhat.utils.filters

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLUtils
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.utils.filters.implementations.VideoFilter
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoFilterProcessor @Inject constructor() : VideoProcessor {

    private var eglContext: EglBase.Context? = null
    private var currentFilter: Filter? = null
    private var glRectDrawer: GlRectDrawer? = null
    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private var sink: VideoSink? = null

    // Shaders para procesamiento de video
    private var vertexShader = -1
    private var fragmentShader = -1
    private var program = -1

    // Buffers para vértices y coordenadas de textura
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer

    companion object {
        private const val TAG = "VideoFilterProcessor"

        // Vértices para el quad donde se renderiza el video
        private val VERTEX_COORDINATES = floatArrayOf(
            -1.0f, -1.0f,  // Inferior izquierda
            1.0f, -1.0f,   // Inferior derecha
            -1.0f, 1.0f,   // Superior izquierda
            1.0f, 1.0f     // Superior derecha
        )

        // Coordenadas de textura
        private val TEXTURE_COORDINATES = floatArrayOf(
            0.0f, 0.0f,  // Inferior izquierda
            1.0f, 0.0f,  // Inferior derecha
            0.0f, 1.0f,  // Superior izquierda
            1.0f, 1.0f   // Superior derecha
        )

        // Vertex shader base
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = aPosition;
                vTextureCoord = aTextureCoord;
            }
        """

        // Fragment shader base
        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform sampler2D sTexture;
            uniform float uIntensity;
            
            // Funciones para ajuste de brillo, contraste y saturación
            vec3 adjustBrightness(vec3 color, float brightness) {
                return color * (1.0 + brightness);
            }
            
            vec3 adjustContrast(vec3 color, float contrast) {
                return (color - 0.5) * contrast + 0.5;
            }
            
            vec3 adjustSaturation(vec3 color, float saturation) {
                float gray = dot(color, vec3(0.299, 0.587, 0.114));
                return mix(vec3(gray), color, saturation);
            }
            
            void main() {
                vec4 color = texture2D(sTexture, vTextureCoord);
                vec3 finalColor = color.rgb;
                finalColor = adjustBrightness(finalColor, uIntensity * 0.5);
                finalColor = adjustContrast(finalColor, 1.0 + uIntensity);
                finalColor = adjustSaturation(finalColor, 1.0 + uIntensity * 0.5);
                gl_FragColor = vec4(finalColor, color.a);
            }
        """
    }

    override fun onCapturerStarted(success: Boolean) {
        Timber.d("Capturer started: $success")
        if (!success) {
            release()
        }
    }

    override fun onCapturerStopped() {
        Timber.d("Capturer stopped")
        release()
    }

    override fun setSink(sink: VideoSink?) {
        this.sink = sink
    }

    override fun onFrameCaptured(videoFrame: VideoFrame) {
        if (currentFilter == null) {
            // Si no hay filtro activo, pasar el frame directamente al sink
            sink?.onFrame(videoFrame)
            return
        }

        try {
            // Crear nuevo frame con el filtro aplicado
            val filteredBuffer = applyFilter(videoFrame.buffer)
            val processedFrame = VideoFrame(filteredBuffer, videoFrame.rotation, videoFrame.timestampNs)
            sink?.onFrame(processedFrame)
            processedFrame.release()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error processing video frame")
            sink?.onFrame(videoFrame)
        }
    }

    fun initialize(eglContext: EglBase.Context) {
        this.eglContext = eglContext
        initializeGl()
    }

    private fun initializeGl() {
        // Inicializar buffers
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(VERTEX_COORDINATES)
                position(0)
            }

        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(TEXTURE_COORDINATES)
                position(0)
            }

        // Compilar shaders
        vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        // Crear y linkear programa
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        // Crear textura
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        surfaceTexture = SurfaceTexture(textureId)
        glRectDrawer = GlRectDrawer()
    }

    fun setFilter(filter: Filter) {
        currentFilter = filter
        (filter as? VideoFilter)?.getCustomShader()?.let { customShader ->
            fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, customShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
        }
    }

    private fun applyFilter(buffer: VideoFrame.Buffer): VideoFrame.Buffer {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val textureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
        val intensityHandle = GLES20.glGetUniformLocation(program, "uIntensity")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureCoordHandle)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)

        // Usar la propiedad intensity del filtro (en lugar de getIntensity())
        GLES20.glUniform1f(intensityHandle, (currentFilter as? VideoFilter)?.intensity ?: 1.0f)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureCoordHandle)

        return createBufferFromTexture(buffer.width, buffer.height)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error compiling shader: $error")
        }
        return shader
    }

    private fun createBufferFromTexture(width: Int, height: Int): VideoFrame.Buffer {
        return TextureBufferImpl(width, height, textureId, eglContext!!)
    }

    fun release() {
        try {
            GLES20.glDeleteProgram(program)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
            surfaceTexture?.release()
            glRectDrawer?.release()
            eglContext = null
            currentFilter = null
            glRectDrawer = null
            surfaceTexture = null
            sink = null
            program = -1
            vertexShader = -1
            fragmentShader = -1
            textureId = -1
        } catch (e: Exception) {
            Timber.e(e, "Error releasing resources")
        }
    }
}
