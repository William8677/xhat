/*
 * Updated: 2025-02-06 01:54:17
 * Author: William8677
 */

package com.williamfq.xhat.ui.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ChatBottomBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onWalkieTalkiePressed: () -> Unit,
    onWalkieTalkieReleased: () -> Unit,
    isWalkieTalkieActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 4.dp,
        modifier = modifier
    ) {
        Column {
            // Indicador de Walkie-Talkie activo
            AnimatedVisibility(
                visible = isWalkieTalkieActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hablando...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de adjuntar
                IconButton(onClick = onAttachmentClick) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Adjuntar archivo"
                    )
                }

                // Campo de texto
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mensaje") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Botón de Walkie-Talkie
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onWalkieTalkiePressed()
                                    tryAwaitRelease()
                                    onWalkieTalkieReleased()
                                }
                            )
                        },
                    shape = MaterialTheme.shapes.small,
                    color = if (isWalkieTalkieActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isWalkieTalkieActive)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = if (isWalkieTalkieActive)
                                "Soltar para enviar"
                            else
                                "Mantener para hablar"
                        )
                    }
                }

                // Botón de enviar mensaje
                IconButton(
                    onClick = onSendClick,
                    enabled = message.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (message.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(
                        imageVector = if (message.isBlank())
                            Icons.Default.Mic
                        else
                            Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (message.isBlank())
                            "Grabar audio"
                        else
                            "Enviar mensaje"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatBottomBarPreview() {
    MaterialTheme {
        ChatBottomBar(
            message = "Mensaje de prueba",
            onMessageChange = {},
            onSendClick = {},
            onAttachmentClick = {},
            onWalkieTalkiePressed = {},
            onWalkieTalkieReleased = {},
            isWalkieTalkieActive = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatBottomBarActivePreview() {
    MaterialTheme {
        ChatBottomBar(
            message = "",
            onMessageChange = {},
            onSendClick = {},
            onAttachmentClick = {},
            onWalkieTalkiePressed = {},
            onWalkieTalkieReleased = {},
            isWalkieTalkieActive = true
        )
    }
}