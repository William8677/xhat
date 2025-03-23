/*
 * Updated: 2025-01-21 23:04:33
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.call.recording.RecordingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CallRecordingButton(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isRecordingBlinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(recordingState) {
        if (recordingState is RecordingState.Recording) {
            scope.launch {
                while (true) {
                    isRecordingBlinking = !isRecordingBlinking
                    delay(500) // Parpadeo cada 500ms
                }
            }
        }
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = {
                when (recordingState) {
                    is RecordingState.Idle -> showConfirmDialog = true
                    is RecordingState.Recording -> onStopRecording()
                    else -> {} // No hacer nada en otros estados
                }
            }
        ) {
            Icon(
                imageVector = when (recordingState) {
                    is RecordingState.Recording -> Icons.Default.FiberManualRecord
                    else -> Icons.Default.RadioButtonUnchecked
                },
                contentDescription = "Grabar llamada",
                tint = when {
                    recordingState is RecordingState.Recording && isRecordingBlinking ->
                        MaterialTheme.colorScheme.error
                    recordingState is RecordingState.Recording ->
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Iniciar grabación") },
                text = {
                    Text(
                        "¿Deseas grabar esta llamada? " +
                                "Asegúrate de tener el consentimiento de todos los participantes."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            onStartRecording()
                        }
                    ) {
                        Text("Iniciar grabación")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun RecordingIndicator(
    recordingState: RecordingState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = recordingState is RecordingState.Recording,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Grabando",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}