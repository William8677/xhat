package com.williamfq.xhat.ui.stories.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.williamfq.domain.model.Story
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun StoryAR(story: Story) {
    var isActive by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isActive) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = "AR Experience",
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer(rotationZ = rotation)
                    .clickable { isActive = !isActive },
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = story.arInstructions ?: "Experiencia AR inmersiva",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        Timber.d("Iniciando experiencia AR para: ${story.id}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Explorar en AR")
            }
        }
    }
}