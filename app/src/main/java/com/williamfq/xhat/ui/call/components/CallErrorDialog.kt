/*
 * Updated: 2025-01-21 22:57:09
 * Author: William8677
 */

package com.williamfq.xhat.ui.call.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.williamfq.xhat.call.model.CallErrorCode

@Composable
fun CallErrorDialog(
    error: CallErrorCode,
    message: String,
    isRecoverable: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error en la llamada") },
        text = { Text(message) },
        confirmButton = {
            if (isRecoverable) {
                Button(
                    onClick = onRetry
                ) {
                    Text("Reintentar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(if (isRecoverable) "Cancelar" else "Aceptar")
            }
        }
    )
}