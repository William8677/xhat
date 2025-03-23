package com.williamfq.xhat.ui.stories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.Story

@Composable
fun TopControls(
    story: Story,
    onDismiss: () -> Unit,
    onPauseToggle: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    isFlashOn: Boolean = false,
    onFlashToggle: () -> Unit = {},
    onSwitchCamera: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            Row {
                IconButton(onClick = onPauseToggle) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = Color.White
                    )
                }

                if (story.mediaType.toString().contains("VIDEO")) {
                    IconButton(onClick = onFlashToggle) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = onSwitchCamera) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,  // Cambiado de FlipCamera a Cameraswitch
                            contentDescription = "Cambiar cámara",
                            tint = Color.White
                        )
                    }
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onReport) {
                    Icon(
                        imageVector = Icons.Default.Report,
                        contentDescription = "Reportar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}