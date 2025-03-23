/*
 * Updated: 2025-01-21 22:57:58
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.call.model.CallStats
import com.williamfq.xhat.call.network.NetworkQuality

@Composable
fun CallStatsOverlay(
    stats: CallStats,
    networkQuality: NetworkQuality,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = formatDuration(stats.duration),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )

        if (stats.resolution != null) {
            Text(
                text = stats.resolution,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }

        Text(
            text = "${stats.bitrate} Kbps",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = getColorForNetworkQuality(networkQuality)
        )

        if (stats.packetLoss > 0) {
            Text(
                text = "Loss: ${String.format("%.1f", stats.packetLoss)}%",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = getColorForPacketLoss(stats.packetLoss)
            )
        }

        Text(
            text = "Ping: ${stats.latency}ms",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = getColorForLatency(stats.latency)
        )
    }
}

private fun getColorForNetworkQuality(quality: NetworkQuality): Color {
    return when (quality) {
        NetworkQuality.EXCELLENT -> Color.Green
        NetworkQuality.GOOD -> Color(0xFF90EE90)
        NetworkQuality.FAIR -> Color.Yellow
        NetworkQuality.POOR -> Color.Red
        NetworkQuality.NO_INTERNET -> Color.Red
    }
}

private fun getColorForPacketLoss(packetLoss: Float): Color {
    return when {
        packetLoss < 1 -> Color.Green
        packetLoss < 3 -> Color.Yellow
        else -> Color.Red
    }
}

private fun getColorForLatency(latency: Int): Color {
    return when {
        latency < 100 -> Color.Green
        latency < 200 -> Color.Yellow
        else -> Color.Red
    }
}

private fun formatDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60)) % 60
    val hours = duration / (1000 * 60 * 60)
    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}