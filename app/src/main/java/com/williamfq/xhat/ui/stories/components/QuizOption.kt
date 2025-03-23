package com.williamfq.xhat.ui.stories.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QuizOption(
    option: String,
    isSelected: Boolean,
    isCorrect: Boolean? = null,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCorrect == true -> Color.Green.copy(alpha = 0.2f)
        isCorrect == false -> Color.Red.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val borderColor = when {
        isCorrect == true -> Color.Green
        isCorrect == false -> Color.Red
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.White.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isCorrect == null) { onSelect() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            AnimatedVisibility(
                visible = isCorrect != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = if (isCorrect == true) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrect == true) "Correcto" else "Incorrecto",
                    tint = if (isCorrect == true) Color.Green else Color.Red
                )
            }
        }
    }
}