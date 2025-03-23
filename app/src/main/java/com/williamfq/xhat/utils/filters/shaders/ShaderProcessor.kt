/*
 * Updated: 2025-01-21 19:59:16
 * Author: William8677
 */

package com.williamfq.xhat.utils.filters.shaders

import android.opengl.GLES20
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShaderProcessor @Inject constructor() {
    companion object {
        // Vertex shader base para todos los efectos
        const val BASE_VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord;
            varying vec2 vTextureCoord;
            
            void main() {
                gl_Position = aPosition;
                vTextureCoord = aTextureCoord;
            }
        """

        // Fragment shaders para diferentes efectos
        val SHADER_MAP = mapOf(
            "beauty" to """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform float uIntensity;
                uniform float uSmoothness;
                uniform float uBrightness;
                
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    
                    // Suavizado de piel
                    vec2 singlePixelStep = vec2(1.0 / 1080.0, 1.0 / 1920.0);
                    vec4 blur = vec4(0.0);
                    float total = 0.0;
                    
                    for(float x = -2.0; x <= 2.0; x++) {
                        for(float y = -2.0; y <= 2.0; y++) {
                            vec2 offset = vec2(x, y) * singlePixelStep;
                            float weight = 1.0 - length(offset) * 0.5;
                            blur += texture2D(sTexture, vTextureCoord + offset) * weight;
                            total += weight;
                        }
                    }
                    blur = blur / total;
                    
                    // Mezclar original con suavizado
                    vec4 smoothed = mix(color, blur, uSmoothness * uIntensity);
                    
                    // Ajustar brillo
                    smoothed.rgb *= (1.0 + uBrightness * uIntensity);
                    
                    gl_FragColor = smoothed;
                }
            """,

            "rainbow" to """
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
            """,

            "glitter" to """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform float uTime;
                uniform float uIntensity;
                
                float random(vec2 st) {
                    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
                }
                
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    vec2 pixelated = floor(vTextureCoord * 50.0) / 50.0;
                    float sparkle = random(pixelated + uTime);
                    
                    if(sparkle > 0.98) {
                        color.rgb += vec3(0.5) * uIntensity;
                    }
                    
                    gl_FragColor = color;
                }
            """,

            "neon" to """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform float uIntensity;
                
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    vec3 neon = pow(color.rgb, vec3(0.6, 0.7, 1.0));
                    neon *= 1.5;
                    neon += vec3(0.1, 0.1, 0.3) * uIntensity;
                    gl_FragColor = vec4(neon, color.a);
                }
            """,

            "galaxy" to """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform float uTime;
                uniform float uIntensity;
                
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    
                    // Crear efecto de estrellas
                    vec2 position = vTextureCoord * 2.0 - 1.0;
                    float angle = atan(position.y, position.x);
                    float radius = length(position);
                    
                    float stars = sin(radius * 50.0 + uTime) * 0.5 + 0.5;
                    stars *= smoothstep(0.5, 0.0, radius);
                    
                    // Colores galácticos
                    vec3 galaxy = vec3(0.1, 0.2, 0.5) + 
                                vec3(0.5, 0.0, 0.5) * radius +
                                vec3(stars);
                                
                    gl_FragColor = vec4(mix(color.rgb, galaxy, uIntensity * 0.7), color.a);
                }
            """,

            "hearts" to """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform float uTime;
                uniform float uIntensity;
                
                float heart(vec2 st, float size) {
                    st = (st - vec2(0.5)) * size;
                    float r = length(st) * 5.0;
                    st = normalize(st);
                    return pow(r - sin(atan(st.y, st.x) * 2.0), 2.0);
                }
                
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    vec2 st = vTextureCoord;
                    
                    float h = heart(st + vec2(sin(uTime) * 0.1, cos(uTime) * 0.1), 2.0);
                    vec3 heartColor = vec3(1.0, 0.0, 0.3) * (1.0 - smoothstep(0.4, 0.41, h));
                    
                    gl_FragColor = vec4(mix(color.rgb, heartColor, uIntensity * heartColor.r), color.a);
                }
            """
        )
    }

    private var currentProgram: Int = 0
    private var currentShaderType: String = ""

    fun setShader(type: String) {
        if (type != currentShaderType) {
            currentShaderType = type
            createProgram(type)
        }
    }

    private fun createProgram(type: String) {
        // Compilar vertex shader
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, BASE_VERTEX_SHADER)

        // Compilar fragment shader
        val fragmentShader = compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            SHADER_MAP[type] ?: throw IllegalArgumentException("Shader type not found: $type")
        )

        // Crear y linkear programa
        currentProgram = GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)

            // Verificar linkeo exitoso
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val error = GLES20.glGetProgramInfoLog(program)
                GLES20.glDeleteProgram(program)
                throw RuntimeException("Error linking program: $error")
            }
        }

        // Limpiar shaders
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
    }

    private fun compileShader(type: Int, source: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                throw RuntimeException("Error compiling shader: $error")
            }
        }
    }

    fun useProgram() {
        GLES20.glUseProgram(currentProgram)
    }

    fun setUniform1f(name: String, value: Float) {
        val location = GLES20.glGetUniformLocation(currentProgram, name)
        GLES20.glUniform1f(location, value)
    }

    fun release() {
        GLES20.glDeleteProgram(currentProgram)
    }
}