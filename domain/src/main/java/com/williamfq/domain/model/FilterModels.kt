/*
 * Updated: 2025-01-23 02:08:23
 * Author: William8677
 */

package com.williamfq.domain.model

import androidx.annotation.DrawableRes

// 1. Corregir la declaración del enum
enum class FilterCategory {
    BEAUTY,      // Filtros de belleza
    FUN,         // Filtros AR divertidos
    ARTISTIC,    // Filtros artísticos
    EFFECTS      // Efectos especiales
}

// 2. Corregir la declaración de la data class
data class FilterType(
    val id: String,
    val name: String,
    val description: String,
    val category: FilterCategory,
    @DrawableRes val previewUrl: Int,
    val parameters: Map<String, Any> = emptyMap(),
    val intensity: Float = 1.0f,
    val isCustom: Boolean = false
)

data class FilterPreset(
    val id: String,
    val name: String,
    val filterId: String,
    val parameters: Map<String, Any>,
    val isDefault: Boolean = false
)

// 3. Corregir la declaración de sealed class
sealed class FilterEffect {
    data class Beauty(
        val smoothing: Float,
        val brightness: Float,
        val contrast: Float,
        val glow: Float = 0f
    ) : FilterEffect()

    data class Fun(
        val arEffectId: String,
        val animated: Boolean = false,
        val animationSpeed: Float = 1f
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

data class FilterResult(
    val success: Boolean,
    val filterId: String,
    val appliedEffect: FilterEffect,
    val processingTime: Long,
    val error: String? = null
)

data class FilterUiState(
    val selectedFilter: FilterType? = null,
    val availableFilters: List<FilterType> = emptyList(),
    val presets: List<FilterPreset> = emptyList(),
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

// 4. Corregir la declaración de las excepciones y agregar override
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

// 5. Corregir el uso de toLowerCase() deprecated
fun FilterType.isAnimated(): Boolean =
    parameters["animated"] as? Boolean ?: false

fun FilterType.getEffectType(): FilterEffect = when(category) {
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
        effectType = name.lowercase(), // Reemplazado toLowerCase() por lowercase()
        parameters = parameters
    )
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