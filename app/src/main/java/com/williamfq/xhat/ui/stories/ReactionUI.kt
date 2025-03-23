/*
 * Updated: 2025-02-08 03:41:44
 * Author: William8677
 */
package com.williamfq.xhat.ui.stories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReactionUI(
    val icon: ImageVector,
    val description: String
) {
    LIKE(Icons.Default.ThumbUp, "Me gusta"),
    LOVE(Icons.Default.Favorite, "Me encanta"),
    LAUGH(Icons.Default.EmojiEmotions, "Me divierte"),
    WOW(Icons.Default.Star, "Me asombra"),
    SAD(Icons.Default.SentimentDissatisfied, "Me entristece"),
    ANGRY(Icons.Default.Mood, "Me enoja"),
    SUPPORT(Icons.Default.ThumbUp, "Apoyo"),
    CELEBRATE(Icons.Default.Celebration, "Celebro");

    companion object {
        fun values(): List<ReactionUI> = values().toList()
    }
}