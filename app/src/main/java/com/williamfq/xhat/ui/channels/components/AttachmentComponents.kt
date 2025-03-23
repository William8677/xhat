/*
 * Updated: 2025-01-22 01:47:22
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.williamfq.xhat.domain.model.ChannelAttachment
import com.williamfq.xhat.domain.model.ChannelContentType
import java.text.DecimalFormat

@Composable
fun AttachmentList(
    attachments: List<ChannelAttachment>,
    onRemoveAttachment: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(attachments) { index, attachment ->
            AttachmentPreview(
                attachment = attachment,
                onRemove = { onRemoveAttachment(index) }
            )
        }
    }
}

@Composable
fun AttachmentPreview(
    attachment: ChannelAttachment,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Box {
            when (attachment.type) {
                ChannelContentType.IMAGE -> {
                    AsyncImage(
                        model = attachment.url,
                        contentDescription = attachment.description,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                ChannelContentType.VIDEO -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = attachment.thumbnailUrl ?: attachment.url,
                            contentDescription = attachment.description,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (attachment.type) {
                                ChannelContentType.AUDIO -> Icons.Default.AudioFile
                                ChannelContentType.FILE -> Icons.Default.InsertDriveFile
                                else -> Icons.Default.AttachFile
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        attachment.size?.let { size ->
                            Text(
                                text = formatFileSize(size),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Botón de eliminar
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = size.toDouble()
    var unit = 0
    while (value > 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return "${DecimalFormat("#.#").format(value)} ${units[unit]}"
}