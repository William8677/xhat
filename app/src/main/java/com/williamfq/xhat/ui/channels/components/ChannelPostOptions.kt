/*
 * Updated: 2025-01-22 01:47:22
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.domain.model.ChannelPost

@Composable
fun PostOptionsMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onReport: () -> Unit = {},
    isAdmin: Boolean = false,
    isPinned: Boolean = false
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, 8.dp)
    ) {
        if (isAdmin) {
            DropdownMenuItem(
                text = { Text(if (isPinned) "Desanclar" else "Anclar") },
                onClick = {
                    onPin()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = null
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Eliminar") },
                onClick = {
                    onDelete()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Reportar") },
                onClick = {
                    onReport()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(Icons.Default.Flag, contentDescription = null)
                }
            )
        }
    }
}

@Composable
fun DeletePostDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar publicación") },
        text = { Text("¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ReportPostDialog(
    onReport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar publicación") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Razón del reporte") },
                maxLines = 3
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onReport(reason)
                    onDismiss()
                },
                enabled = reason.isNotBlank()
            ) {
                Text("Reportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}