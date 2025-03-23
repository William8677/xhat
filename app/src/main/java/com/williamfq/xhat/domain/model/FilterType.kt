/*
 * Updated: 2025-02-12 17:56:46
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

import androidx.annotation.DrawableRes
import com.williamfq.xhat.R

enum class FilterCategory {
    BEAUTY,      // Filtros de belleza
    FUN,         // Filtros AR divertidos
    ARTISTIC,    // Filtros artísticos
    EFFECTS      // Efectos especiales
}

enum class FilterType(
    val id: String,
    val description: String,
    val category: FilterCategory,
    @DrawableRes val previewUrl: Int,
    val parameters: Map<String, Any> = emptyMap(),
    val intensity: Float = 1.0f,
    val isCustom: Boolean = false
) {
    // Filtros de efectos
    GLOW(
        id = "glow",
        description = "Adds a glow effect",
        category = FilterCategory.EFFECTS,
        previewUrl = R.drawable.filters_effects_glow,
        parameters = mapOf(
            "intensity" to 1.0f,
            "radius" to 10.0f
        )
    ),
    BLUR(
        id = "blur",
        description = "Applies a blur effect",
        category = FilterCategory.EFFECTS,
        previewUrl = R.drawable.filters_effects_blur,
        parameters = mapOf(
            "intensity" to 1.0f,
            "radius" to 5.0f
        )
    ),

    // Filtros artísticos
    SEPIA(
        id = "sepia",
        description = "Applies a sepia tone",
        category = FilterCategory.ARTISTIC,
        previewUrl = R.drawable.filters_artistic_sepia,
        parameters = mapOf(
            "intensity" to 1.0f,
            "contrast" to 1.0f
        )
    ),
    GRAYSCALE(
        id = "grayscale",
        description = "Converts the image to grayscale",
        category = FilterCategory.ARTISTIC,
        previewUrl = R.drawable.filters_artistic_grayscale,
        parameters = mapOf(
            "intensity" to 1.0f,
            "contrast" to 1.0f
        )
    ),

    // Filtros de belleza
    BEAUTY(
        id = "beauty",
        description = "Mejora natural del rostro",
        category = FilterCategory.BEAUTY,
        previewUrl = R.drawable.ic_filter_beauty,
        parameters = mapOf(
            "intensity" to 0.5f,
            "smoothing" to 0.5f,
            "brightness" to 0.0f
        )
    ),

    // Filtros AR
    FUN(
        id = "fun",
        description = "Efectos divertidos AR",
        category = FilterCategory.FUN,
        previewUrl = R.drawable.ic_filter_fun,
        parameters = mapOf(
            "intensity" to 1.0f,
            "animated" to true,
            "arEffect" to "default"
        )
    ),

    // Filtros especiales
    RAINBOW(
        id = "rainbow",
        description = "Efecto arcoíris brillante",
        category = FilterCategory.EFFECTS,
        previewUrl = R.drawable.ic_filter_rainbow,
        parameters = mapOf(
            "intensity" to 0.7f,
            "speed" to 0.5f,
            "colorCycles" to 1.0f
        )
    );

    companion object {
        fun fromId(id: String): FilterType = values().find { it.id == id }
            ?: throw FilterException.InvalidFilterId(id)
    }
}

sealed class FilterEffect {
    data class Beauty(
        val smoothing: Float = 0.5f,
        val brightness: Float = 0.0f,
        val contrast: Float = 0.0f,
        val glow: Float = 0f
    ) : FilterEffect()

    data class Fun(
        val arEffectId: String,
        val animated: Boolean = false,
        val animationSpeed: Float = 1.0f
    ) : FilterEffect()

    data class Artistic(
        val intensity: Float,
        val brushSize: Float? = null,
        val edgeThreshold: Float? = null,
        val blendMode: String? = null
    ) : FilterEffect()

    data class Special(
        val effectType: String,
        val parameters: Map<String, Any>
    ) : FilterEffect()
}

data class FilterState(
    val currentFilter: FilterType? = null,
    val intensity: Float = 0.5f,
    val isEnabled: Boolean = false,
    val effect: FilterEffect? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)

sealed class FilterEvent {
    data class SelectFilter(val filter: FilterType?) : FilterEvent()
    data class UpdateParameter(val name: String, val value: Any) : FilterEvent()
    data class SavePreset(val name: String) : FilterEvent()
    data class LoadPreset(val presetId: String) : FilterEvent()
    object ClearFilter : FilterEvent()
    object TogglePreview : FilterEvent()
    object DismissError : FilterEvent()
}

sealed class FilterException : Exception() {
    data class InvalidFilterId(val filterId: String) : FilterException() {
        override val message: String = "Invalid filter ID: $filterId"
    }
    data class InvalidParameter(val paramName: String, val value: Any) : FilterException() {
        override val message: String = "Invalid parameter '$paramName' with value: $value"
    }
    data class ProcessingError(override val message: String) : FilterException()
    object UnsupportedEffect : FilterException() {
        private fun readResolve(): Any = UnsupportedEffect
        override val message: String = "Unsupported effect type"
    }
}

object FilterConstants {
    const val MAX_SMOOTHING = 1.0f
    const val MAX_BRIGHTNESS = 0.5f
    const val MAX_CONTRAST = 2.0f
    const val MAX_GLOW = 1.0f
    const val DEFAULT_ANIMATION_SPEED = 1.0f
    const val MAX_ARTISTIC_INTENSITY = 1.0f
    const val MAX_BRUSH_SIZE = 10.0f
    const val MAX_EDGE_THRESHOLD = 1.0f

    val SUPPORTED_BLEND_MODES = listOf(
        "normal",
        "multiply",
        "screen",
        "overlay",
        "soft_light",
        "hard_light"
    )
}

fun FilterType.isAnimated(): Boolean =
    parameters["animated"] as? Boolean ?: false

fun FilterType.getEffectType(): FilterEffect = when (category) {
    FilterCategory.BEAUTY -> FilterEffect.Beauty(
        smoothing = parameters["smoothing"] as? Float ?: 0.5f,
        brightness = parameters["brightness"] as? Float ?: 0f,
        contrast = parameters["contrast"] as? Float ?: 1f,
        glow = parameters["glow"] as? Float ?: 0f
    )
    FilterCategory.FUN -> FilterEffect.Fun(
        arEffectId = parameters["arEffect"] as? String ?: "",
        animated = parameters["animated"] as? Boolean ?: false,
        animationSpeed = parameters["animationSpeed"] as? Float ?: 1f
    )
    FilterCategory.ARTISTIC -> FilterEffect.Artistic(
        intensity = parameters["artisticIntensity"] as? Float ?: 0.5f,
        brushSize = parameters["brushSize"] as? Float,
        edgeThreshold = parameters["edgeThreshold"] as? Float,
        blendMode = parameters["blendMode"] as? String
    )
    FilterCategory.EFFECTS -> FilterEffect.Special(
        effectType = name.lowercase(),
        parameters = parameters
    )
}