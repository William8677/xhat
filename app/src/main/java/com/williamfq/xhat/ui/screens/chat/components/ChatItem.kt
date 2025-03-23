/*
 * Updated: 2025-02-08 03:35:08
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.williamfq.xhat.ui.screens.chat.model.ChatPreview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatItem(
    chat: ChatPreview,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar con todas las funcionalidades
            ChatAvatar(
                photoUrl = chat.photoUrl,
                name = chat.title,
                isGroup = chat.isGroup,
                avatarType = chat.avatarType,
                groupAvatarUrls = chat.getGroupAvatarGrid(),
                customEmoji = chat.customAvatarEmoji,
                backgroundColor = Color(android.graphics.Color.parseColor(chat.getAvatarBackgroundColor()))
            )

            // Contenido principal
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Título del chat
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Indicador de silenciado
                    if (chat.isMuted) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Último mensaje con nombre del remitente en grupos
                Text(
                    text = buildLastMessageText(chat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tiempo y notificaciones
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Timestamp
                Text(
                    text = formatChatTime(chat.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Badge de mensajes no leídos
                if (chat.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(
    photoUrl: String?,
    name: String,
    isGroup: Boolean,
    avatarType: ChatPreview.AvatarType,
    groupAvatarUrls: List<String>,
    customEmoji: String?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        when (avatarType) {
            ChatPreview.AvatarType.CUSTOM_IMAGE -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Avatar de $name",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            ChatPreview.AvatarType.GROUP_GRID -> {
                GroupAvatarGrid(
                    avatarUrls = groupAvatarUrls,
                    groupName = name
                )
            }
            ChatPreview.AvatarType.EMOJI -> {
                Text(
                    text = customEmoji ?: "👤",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .align(Alignment.Center)
                )
            }
            ChatPreview.AvatarType.INITIALS -> {
                Text(
                    text = name.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }.uppercase(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .align(Alignment.Center)
                )
            }
            else -> {
                Icon(
                    imageVector = if (isGroup) Icons.Default.Group else Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupAvatarGrid(
    avatarUrls: List<String>,
    groupName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (avatarUrls.size) {
            0 -> {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            1 -> {
                AsyncImage(
                    model = avatarUrls[0],
                    contentDescription = "Avatar de grupo $groupName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        avatarUrls.take(2).forEach { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(1.dp)
                            )
                        }
                    }
                    if (avatarUrls.size > 2) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            avatarUrls.drop(2).take(2).forEach { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildLastMessageText(chat: ChatPreview): String {
    return if (chat.isGroup && chat.lastMessageSenderName != null) {
        "${chat.lastMessageSenderName}: ${chat.lastMessage}"
    } else {
        chat.lastMessage
    }
}

private fun formatChatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1 -> {
            "Ayer"
        }
        now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatItemPreview() {
    MaterialTheme {
        Column {
            ChatItem(
                chat = ChatPreview(
                    id = "1",
                    title = "Juan Pérez",
                    lastMessage = "¡Hola! ¿Cómo estás?",
                    timestamp = System.currentTimeMillis(),
                    unreadCount = 3,
                    photoUrl = null,
                    avatarType = ChatPreview.AvatarType.INITIALS
                ),
                onClick = {}
            )

            HorizontalDivider()

            ChatItem(
                chat = ChatPreview(
                    id = "2",
                    title = "Grupo Familia",
                    lastMessage = "¡Feliz cumpleaños! 🎉",
                    timestamp = System.currentTimeMillis() - 86400000,
                    unreadCount = 0,
                    isGroup = true,
                    lastMessageSenderName = "María",
                    avatarType = ChatPreview.AvatarType.GROUP_GRID,
                    groupAvatarUrls = listOf(
                        "https://example.com/avatar1.jpg",
                        "https://example.com/avatar2.jpg",
                        "https://example.com/avatar3.jpg"
                    )
                ),
                onClick = {}
            )
        }
    }
}