/*
 * Updated: 2025-02-06 02:01:31
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Actualizamos para usar ChatMessage en lugar de Message
import com.williamfq.domain.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Username
        Text(
            text = if (isCurrentUser) "Tú" else message.username, // Se usa username
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Message bubble
        Card(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isCurrentUser) 16.dp else 0.dp,
                        topEnd = if (isCurrentUser) 0.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Message content
                Text(
                    text = message.content,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Timestamp
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }

    return when {
        // Si es hoy
        now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Si es ayer
        now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1 -> {
            "Ayer " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Si es esta semana
        now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(date)
        }
        // Si es este año
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(date)
        }
        // Si es otro año
        else -> {
            SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(date)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatMessageItemPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChatMessageItem(
                message = ChatMessage(
                    id = 0,
                    messageId = "1",
                    chatId = "chat1",
                    content = "¡Hola! ¿Cómo estás?",
                    senderId = "user1",
                    recipientId = "user2",
                    username = "Juan", // Se usa username
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                onClick = {},
                onLongPress = {},
                isCurrentUser = false
            )

            ChatMessageItem(
                message = ChatMessage(
                    id = 0,
                    messageId = "2",
                    chatId = "chat1",
                    content = "¡Bien! ¿Y tú? 😊",
                    senderId = "currentUser",
                    recipientId = "user1",
                    username = "Tú", // Se usa username
                    timestamp = System.currentTimeMillis()
                ),
                onClick = {},
                onLongPress = {},
                isCurrentUser = true
            )
        }
    }
}
