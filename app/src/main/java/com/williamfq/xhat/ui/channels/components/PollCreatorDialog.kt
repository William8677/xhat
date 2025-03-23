/*
 * Updated: 2025-01-22 01:46:12
 * Author: William8677
 */
package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.williamfq.xhat.domain.model.ChannelPoll
import com.williamfq.xhat.domain.model.PollOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollCreatorDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (ChannelPoll) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf<Long?>(null) } // opcional, según necesidad

    // Validación: se requiere que la pregunta no esté vacía y que al menos dos opciones no vacías existan.
    val isValid = question.trim().isNotEmpty() &&
            options.count { it.trim().isNotEmpty() } >= 2

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Crear encuesta",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pregunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Opciones de respuesta
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                options = options.toMutableList().apply {
                                    set(index, newValue)
                                }
                            },
                            label = { Text("Opción ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        if (options.size > 2) {
                            IconButton(
                                onClick = {
                                    options = options.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar opción")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                // Botón para agregar más opciones
                TextButton(
                    onClick = {
                        options = options + ""
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    // Si existe un ícono de "Agregar", se podría usar; en este ejemplo reutilizamos Delete como stub.
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar opción")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Alternar si la encuesta permite respuestas múltiples.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isMultipleChoice,
                        onCheckedChange = { isMultipleChoice = it }
                    )
                    Text("Permitir selección múltiple")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // (Opcional) Duración de la encuesta
                Spacer(modifier = Modifier.height(16.dp))
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val validOptions = options.filter { it.trim().isNotEmpty() }
                                .map { PollOption(text = it.trim()) }
                            val poll = ChannelPoll(
                                question = question.trim(),
                                options = validOptions,
                                isMultipleChoice = isMultipleChoice,
                                endsAt = duration?.let { System.currentTimeMillis() + it }
                            )
                            onCreatePoll(poll)
                        },
                        enabled = isValid
                    ) {
                        Text("Crear")
                    }
                }
            }
        }
    }
}
