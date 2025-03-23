package com.williamfq.xhat.ui.stories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class StoryReaction(
    val emoji: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
) {
    LIKE(
        "👍",
        "Me gusta",
        Icons.Filled.ThumbUp,
        Color(0xFF2196F3)
    ),
    LOVE(
        "❤️",
        "Me encanta",
        Icons.Filled.Favorite,
        Color(0xFFE91E63)
    ),
    LAUGH(
        "😄",
        "Me divierte",
        Icons.Filled.EmojiEmotions,
        Color(0xFFFFEB3B)
    ),
    WOW(
        "😮",
        "Me sorprende",
        Icons.Filled.Stars,
        Color(0xFFFF9800)
    ),
    SAD(
        "😢",
        "Me entristece",
        Icons.Filled.SentimentDissatisfied,
        Color(0xFF9E9E9E)
    ),
    ANGRY(
        "😠",
        "Me enoja",
        Icons.Filled.Mood,
        Color(0xFFFF5722)
    ),
    SUPPORT(
        "🤝",
        "Apoyo",
        Icons.Filled.Handshake,  // Cambiado de HandshakeOutlined a Handshake
        Color(0xFF4CAF50)
    ),
    CELEBRATE(
        "🎉",
        "Celebro",
        Icons.Filled.Celebration,
        Color(0xFF9C27B0)
    ),
    FIRE(
        "🔥",
        "Fuego",
        Icons.Filled.LocalFireDepartment,
        Color(0xFFFF5722)
    ),
    CLAP(
        "👏",
        "Aplaudo",
        Icons.Filled.WavingHand,
        Color(0xFFFFEB3B)
    ),
    THINK(
        "🤔",
        "Me hace pensar",
        Icons.Filled.Psychology,
        Color(0xFF03A9F4)
    ),
    EYES(
        "👀",
        "Interesante",
        Icons.Filled.Visibility,
        Color(0xFF673AB7)
    ),
    HUNDRED(
        "💯",
        "Perfecto",
        Icons.Filled.Grade,
        Color(0xFFFFD700)
    ),
    HEART_EYES(
        "😍",
        "Me fascina",
        Icons.Filled.HeartBroken,
        Color(0xFFE91E63)
    ),
    MINDBLOWN(
        "🤯",
        "Increíble",
        Icons.Filled.Lightbulb,
        Color(0xFFFFA500)
    );

    companion object {
        fun fromEmoji(emoji: String): StoryReaction? {
            return entries.find { it.emoji == emoji }
        }

        fun fromDescription(description: String): StoryReaction? {
            return entries.find { it.description == description }
        }
    }
}