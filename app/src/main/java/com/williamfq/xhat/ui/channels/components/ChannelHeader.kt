/*
 * Updated: 2025-01-22 01:45:16
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.williamfq.xhat.domain.model.Channel
import java.text.DecimalFormat

@Composable
fun ChannelHeader(
    channel: Channel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Imagen de portada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = channel.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Badge de canal verificado/oficial
            if (channel.isVerified || channel.isOfficial) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (channel.isOfficial)
                                Icons.Default.Verified
                            else
                                Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (channel.isOfficial) "Oficial" else "Verificado",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Información del canal
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = channel.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Estadísticas
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelStat(
                    icon = Icons.Default.People,
                    value = formatNumber(channel.stats.subscribersCount),
                    label = "Suscriptores"
                )
                ChannelStat(
                    icon = Icons.Default.Article,
                    value = formatNumber(channel.stats.postsCount),
                    label = "Publicaciones"
                )
                ChannelStat(
                    icon = Icons.Default.TrendingUp,
                    value = "${channel.stats.engagement}%",
                    label = "Engagement"
                )
            }

            // Vista previa de configuración (si existen requisitos para suscribirse)
            if (channel.settings.requireApproval ||
                channel.settings.minimumAccountAge > 0 ||
                channel.settings.minimumKarma > 0
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Requisitos para suscribirse:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (channel.settings.requireApproval) {
                            Text("• Requiere aprobación del administrador")
                        }
                        if (channel.settings.minimumAccountAge > 0) {
                            Text("• Cuenta de ${channel.settings.minimumAccountAge} días o más")
                        }
                        if (channel.settings.minimumKarma > 0) {
                            Text("• Mínimo ${channel.settings.minimumKarma} karma")
                        }
                    }
                }
            }
        }

        Divider()
    }
}

@Composable
private fun ChannelStat(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "${DecimalFormat("#.#").format(number / 1_000_000.0)}M"
        number >= 1_000 -> "${DecimalFormat("#.#").format(number / 1_000.0)}K"
        else -> number.toString()
    }
}
