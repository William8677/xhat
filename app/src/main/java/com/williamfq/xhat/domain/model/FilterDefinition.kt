/*
 * Updated: 2025-01-26
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

import androidx.annotation.DrawableRes

/**
 * Representa la definición de un filtro con todos sus atributos necesarios.
 */
data class FilterDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: FilterCategory,
    @DrawableRes val previewUrl: Int,
    val parameters: Map<String, Any> = emptyMap(),
    val intensity: Float = 1.0f
)

fun FilterDefinition.toFilterType(): FilterType {
    return when(name.lowercase()) {
        "glow" -> FilterType.GLOW
        "blur" -> FilterType.BLUR
        "sepia" -> FilterType.SEPIA
        "grayscale" -> FilterType.GRAYSCALE
        // Agrega otros mapeos según corresponda.
        else -> FilterType.BLUR
    }
}

