/*
 * Updated: 2025-01-25 15:43:28
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.preview

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.williamfq.xhat.ui.call.components.*
import com.williamfq.xhat.ui.theme.XhatTheme

class NetworkQualityProvider : PreviewParameterProvider<NetworkQuality> {
    override val values = NetworkQuality.values().asSequence()
}

@Preview(name = "Network Quality Indicator", showBackground = true)
@Composable
fun PreviewNetworkQualityIndicator(
    @PreviewParameter(NetworkQualityProvider::class) quality: NetworkQuality
) {
    XhatTheme {
        NetworkQualityIndicator(quality = quality)
    }
}

@Preview(name = "Call Recording Indicator - Recording", showBackground = true)
@Composable
fun PreviewCallRecordingIndicator() {
    XhatTheme {
        CallRecordingIndicator(isRecording = true)
    }
}

@Preview(name = "Call Stats Overlay", showBackground = true)
@Composable
fun PreviewCallStatsOverlay() {
    XhatTheme {
        CallStatsOverlay(
            stats = CallStats(
                width = 1280,
                height = 720,
                fps = 30,
                bitrate = 2000,
                latency = 150,
                packetLoss = 0.5f
            )
        )
    }
}