package com.williamfq.xhat.ui.stories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.williamfq.domain.model.Story

@Composable
fun BottomControls(
    story: Story,
    commentText: String,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
    onReactionClick: () -> Unit,
    onVoiceRecordingStart: () -> Unit,
    onCameraClick: () -> Unit,
    isCapturing: Boolean = false,
    onCaptureClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onFiltersClick: () -> Unit = {}
) {
    Column(
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
            TextField(
                value = commentText,
                onValueChange = onCommentChange,
                placeholder = { Text("Envía un mensaje...", color = Color.White.copy(alpha = 0.6f)) },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onCommentSend() }),
                shape = CircleShape
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onReactionClick) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Reaccionar",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onVoiceRecordingStart) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Grabar audio",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onCameraClick) {
                    Icon(
                        imageVector = if (isCapturing) Icons.Default.CameraAlt else Icons.Default.Camera,
                        contentDescription = "Cámara",
                        tint = Color.White
                    )
                }
            }
        }

        if (isCapturing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onGalleryClick) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Galería",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onCaptureClick) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = "Capturar",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = onFiltersClick) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filtros",
                        tint = Color.White
                    )
                }
            }
        }
    }
}