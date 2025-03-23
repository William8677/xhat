package com.williamfq.xhat.ui.stories.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.Story

@Composable
fun StoryText(story: Story) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(story.backgroundColor ?: Color.Black)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = story.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = (story.textColor ?: Color.White).copy(alpha = alpha),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}