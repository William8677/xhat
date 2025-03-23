/*
 * Updated: 2025-02-12 17:43:00
 * Author: William8677
 */

package com.williamfq.xhat.utils.filters.implementations

import android.content.Context
import android.graphics.*
import com.williamfq.xhat.R
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.base.Filter

/**
 * Clase base abstracta VideoFilter que extiende Filter.
 * Define métodos abstractos para obtener el nombre, descripción, ícono de vista previa e intensidad,
 * además de otros métodos necesarios para aplicar el filtro, obtener su tipo, parámetros y shader personalizado.
 */
abstract class VideoFilter : Filter() {
    // Se definen métodos abstractos (en lugar de propiedades) para evitar conflictos de firma.
    abstract override fun getName(): String
    abstract override fun getDescription(): String
    abstract fun getPreviewIconRes(): Int
    abstract var intensity: Float

    override abstract fun applyFilter(bitmap: Bitmap): Bitmap
    override abstract fun getType(): FilterType
    override abstract fun getParameters(): Map<String, Any>

    // Método específico para obtener un shader personalizado, si lo hubiera.
    abstract fun getCustomShader(): String?
}

/**
 * Implementación del filtro de belleza.
 * Se simula el suavizado reduciendo la resolución y volviendo a escalar el bitmap.
 */
class BeautyFilter : VideoFilter() {
    override fun getName() = "Belleza"
    override fun getDescription() = "Mejora natural del rostro"
    override fun getPreviewIconRes() = R.drawable.ic_filter_beauty
    override var intensity = 0.5f

    override fun getType(): FilterType = FilterType.BEAUTY

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to intensity,
        "smoothing" to 0.5f,
        "brightness" to 0.0f
    )

    override fun getCustomShader(): String? = """
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform sampler2D sTexture;
        uniform float uIntensity;
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            // Lógica para suavizar
            gl_FragColor = color;
        }
    """

    override fun applyFilter(bitmap: Bitmap): Bitmap {
        // Simula un efecto de suavizado reduciendo la resolución y luego reescalando.
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = 0.25f
        val smallWidth = (width * scaleFactor).toInt().coerceAtLeast(1)
        val smallHeight = (height * scaleFactor).toInt().coerceAtLeast(1)
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, smallWidth, smallHeight, true)
        return Bitmap.createScaledBitmap(smallBitmap, width, height, true)
    }
}

/**
 * Implementación del filtro de diversión.
 * Se aplica un efecto de inversión de colores.
 */
class FunFilter(private val context: Context) : VideoFilter() {
    override fun getName() = "Diversión"
    override fun getDescription() = "Efectos divertidos AR"
    override fun getPreviewIconRes() = R.drawable.ic_filter_fun
    override var intensity = 1.0f

    override fun getType(): FilterType = FilterType.FUN

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to intensity,
        "animated" to true
    )

    // Stub para detector de rostros o procesamiento AR.
    private val faceDetector = null

    override fun getCustomShader(): String? = null

    override fun applyFilter(bitmap: Bitmap): Bitmap {
        // Aplica un efecto de inversión de colores.
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = bitmap.copy(config, true)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val a = Color.alpha(pixel)
                val r = 255 - Color.red(pixel)
                val g = 255 - Color.green(pixel)
                val b = 255 - Color.blue(pixel)
                result.setPixel(x, y, Color.argb(a, r, g, b))
            }
        }
        return result
    }
}

/**
 * Implementación del filtro arcoíris.
 * Se crea una superposición de degradado “arcoíris” que se mezcla con la imagen original.
 */
class RainbowFilter : VideoFilter() {
    override fun getName() = "Arcoíris"
    override fun getDescription() = "Efecto arcoíris brillante"
    override fun getPreviewIconRes() = R.drawable.ic_filter_rainbow
    override var intensity = 0.7f

    override fun getType(): FilterType = FilterType.RAINBOW

    override fun getParameters(): Map<String, Any> = mapOf(
        "intensity" to intensity,
        "speed" to 0.5f
    )

    override fun getCustomShader(): String? = """
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform sampler2D sTexture;
        uniform float uTime;
        uniform float uIntensity;
        vec3 rainbow(float t) {
            vec3 a = vec3(0.5, 0.5, 0.5);
            vec3 b = vec3(0.5, 0.5, 0.5);
            vec3 c = vec3(1.0, 1.0, 1.0);
            vec3 d = vec3(0.00, 0.33, 0.67);
            return a + b * cos(6.28318 * (c * t + d));
        }
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            vec3 rainbowColor = rainbow(vTextureCoord.x + uTime);
            gl_FragColor = vec4(mix(color.rgb, rainbowColor, uIntensity * 0.5), color.a);
        }
    """

    override fun applyFilter(bitmap: Bitmap): Bitmap {
        // Crea una copia mutable del bitmap original usando un config por defecto en caso de ser nulo.
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val result = bitmap.copy(config, true)
        val canvas = Canvas(result)
        val paint = Paint()
        // Define un degradado arcoíris horizontal.
        val rainbowColors = intArrayOf(
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA
        )
        val positions = floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)
        val shader = LinearGradient(
            0f, 0f, result.width.toFloat(), 0f,
            rainbowColors, positions, Shader.TileMode.CLAMP
        )
        paint.shader = shader
        // Ajusta la opacidad según la intensidad (0 a 255).
        paint.alpha = (intensity * 255).toInt().coerceIn(0, 255)
        // Utiliza un modo de fusión para mezclar (OVERLAY).
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), paint)
        return result
    }
}
