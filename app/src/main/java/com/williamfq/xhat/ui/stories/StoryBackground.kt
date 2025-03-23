/*
 * Updated: 2025-02-08 04:00:15
 * Author: William8677
 */
package com.williamfq.xhat.ui.stories

enum class BackgroundType {
    SOLID_COLOR,
    GRADIENT,
    IMAGE,
    ANIMATED
}

data class StoryBackground(
    val type: BackgroundType,
    val value: String, // Color hex o URL
    val gradient: List<String>? = null
)