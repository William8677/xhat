/*
 * Updated: 2025-01-25
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.domain.model.ChannelPost

@Composable
fun ChannelPost(
    post: ChannelPost,
    isAdmin: Boolean,
    onReactionClick: (emoji: String) -> Unit,
    onShareClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Muestra el contenido de la publicación
            Text(text = post.content)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                // Botón para reaccionar (por ejemplo, con "👍")
                TextButton(onClick = { onReactionClick("👍") }) {
                    Text("Reaccionar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Botón para compartir
                TextButton(onClick = onShareClick) {
                    Text("Compartir")
                }
                // Si el usuario es administrador, muestra el botón de opciones
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onOptionsClick) {
                        Text("Opciones")
                    }
                }
            }
        }
    }
}
