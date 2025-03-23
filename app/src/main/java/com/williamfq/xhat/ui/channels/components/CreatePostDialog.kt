/*
 * Updated: 2025-01-22 01:46:12
 * Author: William8677
 */
package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.williamfq.xhat.domain.model.*
import com.williamfq.xhat.ui.channels.components.PollPreview
import com.williamfq.xhat.ui.channels.components.PollCreatorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onCreatePost: (
        content: String,
        type: ChannelContentType,
        attachments: List<ChannelAttachment>,
        poll: ChannelPoll?
    ) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ChannelContentType.TEXT) }
    var attachments by remember { mutableStateOf<List<ChannelAttachment>>(emptyList()) }
    var showPollCreator by remember { mutableStateOf(false) }
    var poll by remember { mutableStateOf<ChannelPoll?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Nueva publicación",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Selector de tipo de contenido
                ContentTypeSelector(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Campo de contenido
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenido") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Adjuntos (si existen)
                if (attachments.isNotEmpty()) {
                    AttachmentList(
                        attachments = attachments,
                        onRemoveAttachment = { index ->
                            attachments = attachments.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Encuesta
                poll?.let {
                    PollPreview(poll = it, onRemovePoll = { poll = null })
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        // Botón de adjuntar archivos
                        IconButton(onClick = { /* TODO: Implementar selección de archivos */ }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar archivo")
                        }
                        // Botón de crear encuesta
                        IconButton(
                            onClick = { showPollCreator = true },
                            enabled = poll == null
                        ) {
                            Icon(Icons.Default.Poll, contentDescription = "Crear encuesta")
                        }
                    }
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onCreatePost(content, selectedType, attachments, poll) },
                            enabled = content.isNotBlank() || attachments.isNotEmpty() || poll != null
                        ) {
                            Text("Publicar")
                        }
                    }
                }
            }
        }
    }
    // Diálogo para crear encuesta
    if (showPollCreator) {
        PollCreatorDialog(
            onDismiss = { showPollCreator = false },
            onCreatePoll = {
                poll = it
                showPollCreator = false
            }
        )
    }
}

@Composable
private fun ContentTypeSelector(
    selectedType: ChannelContentType,
    onTypeSelected: (ChannelContentType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChannelContentType.values().forEach { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        when (type) {
                            ChannelContentType.TEXT -> "Texto"
                            ChannelContentType.IMAGE -> "Imagen"
                            ChannelContentType.VIDEO -> "Video"
                            ChannelContentType.AUDIO -> "Audio"
                            ChannelContentType.FILE -> "Archivo"
                            ChannelContentType.POLL -> "Encuesta"
                            ChannelContentType.LINK -> "Enlace"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (type) {
                            ChannelContentType.TEXT -> Icons.Default.TextFormat
                            ChannelContentType.IMAGE -> Icons.Default.Image
                            ChannelContentType.VIDEO -> Icons.Default.VideoLibrary
                            ChannelContentType.AUDIO -> Icons.Default.MusicNote
                            ChannelContentType.FILE -> Icons.Default.InsertDriveFile
                            ChannelContentType.POLL -> Icons.Default.Poll
                            ChannelContentType.LINK -> Icons.Default.Link
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}
