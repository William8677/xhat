/*
 * Updated: 2025-01-22 01:46:12
 * Author: William8677
 */
package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.domain.model.ChannelPoll

/**
 * Muestra una vista previa de la encuesta, con la pregunta y las opciones listadas.
 * Incluye un botón para eliminar la encuesta, que invoca [onRemovePoll].
 */
@Composable
fun PollPreview(
    poll: ChannelPoll,
    onRemovePoll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título de la encuesta con botón para eliminar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = poll.question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemovePoll) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar encuesta")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Lista de opciones de la encuesta
            poll.options.forEach { option ->
                Text(
                    text = "• ${option.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
