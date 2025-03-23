/*
 * Updated: 2025-01-26 22:52:25
 * Author: William8677
 */

package com.williamfq.xhat.ui.screens.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.ui.screens.chat.model.ChatPreview

@Composable
fun ChatListItem(
    chat: ChatPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(chat.title) },
        supportingContent = { Text(chat.lastMessage) },
        trailingContent = {
            Text(
                text = formatTimestamp(chat.timestamp),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}