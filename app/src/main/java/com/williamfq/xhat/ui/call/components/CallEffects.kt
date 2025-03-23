/*
 * Updated: 2025-01-25 15:43:28
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun NetworkQualityIndicator(
    quality: NetworkQuality,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(true) }

    LaunchedEffect(quality) {
        if (quality == NetworkQuality.POOR) {
            showIndicator = true
            delay(3.seconds)
            showIndicator = false
        }
    }

    AnimatedVisibility(
        visible = showIndicator && quality == NetworkQuality.POOR,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Conexión inestable",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun CallRecordingIndicator(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isRecording,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Color.Red,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Text(
                    text = "Grabando",
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

@Composable
fun CallStatsOverlay(
    stats: CallStats,
    modifier: Modifier = Modifier
) {
    var showStats by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Estadísticas", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showStats = !showStats }) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Toggle Stats"
                    )
                }
            }

            AnimatedVisibility(visible = showStats) {
                Column {
                    StatRow("Resolución", "${stats.width}x${stats.height}")
                    StatRow("FPS", "${stats.fps}")
                    StatRow("Bitrate", "${stats.bitrate} kbps")
                    StatRow("Latencia", "${stats.latency} ms")
                    StatRow("Pérdida de paquetes", "${stats.packetLoss}%")
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class CallStats(
    val width: Int = 0,
    val height: Int = 0,
    val fps: Int = 0,
    val bitrate: Int = 0,
    val latency: Int = 0,
    val packetLoss: Float = 0f
)