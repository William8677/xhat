/*
 * Updated: 2025-01-22 01:43:04
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.williamfq.xhat.domain.model.Channel
import com.williamfq.xhat.data.repository.ChannelFilter
import com.williamfq.xhat.domain.model.ChannelCategory
import java.text.DecimalFormat

@Composable
fun VerifiedChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onSubscribe: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box {
            // Imagen de portada
            AsyncImage(
                model = channel.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            // Contenido del canal
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Información del canal
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = channel.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Estadísticas
                        Row(
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = formatSubscribers(channel.stats.subscribersCount),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${channel.stats.postsCount} publicaciones",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Botón de suscripción
                    Button(
                        onClick = onSubscribe,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text("Suscribirse")
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onSubscribe: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Avatar del canal
            AsyncImage(
                model = channel.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información del canal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = channel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = formatSubscribers(channel.stats.subscribersCount),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${channel.stats.postsCount} publicaciones",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Botón de suscripción
            TextButton(
                onClick = onSubscribe
            ) {
                Text("Suscribirse")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelFilterSheet(
    currentFilter: ChannelFilter,
    onFilterChange: (ChannelFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Categorías
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChannelCategory.values().toList()) { category: ChannelCategory ->
                    FilterChip(
                        selected = currentFilter.category == category,
                        onClick = {
                            onFilterChange(
                                currentFilter.copy(
                                    category = if (currentFilter.category == category) null else category
                                )
                            )
                        },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Idioma
            OutlinedTextField(
                value = currentFilter.language ?: "",
                onValueChange = {
                    onFilterChange(
                        currentFilter.copy(
                            language = it.takeIf { it.isNotEmpty() }
                        )
                    )
                },
                label = { Text("Idioma") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verificados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solo canales verificados")
                Switch(
                    checked = currentFilter.verified,
                    onCheckedChange = {
                        onFilterChange(currentFilter.copy(verified = it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onFilterChange(ChannelFilter())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}

private fun formatSubscribers(count: Int): String {
    return when {
        count >= 1_000_000 -> "${DecimalFormat("#.#").format(count / 1_000_000.0)}M"
        count >= 1_000 -> "${DecimalFormat("#.#").format(count / 1_000.0)}K"
        else -> count.toString()
    } + " suscriptores"
}
