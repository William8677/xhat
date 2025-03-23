package com.williamfq.xhat.ui.stories.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.Story

@Composable
fun StoryInteractiveElements(story: Story) {
    story.interactiveElements?.forEach { element ->
        var isExpanded by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isExpanded) 1.2f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = element.position.x.dp,
                    y = element.position.y.dp
                )
                .scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = element.icon,
                    contentDescription = element.title,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = Color.White
                )
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(start = 48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = element.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = element.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    element.actionLabel?.let { label ->
                        TextButton(
                            onClick = { element.action?.invoke() }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}