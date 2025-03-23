/*
 * Updated: 2025-02-08 02:10:58
 * Author: William8677
 */
package com.williamfq.xhat.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.domain.model.ChatMessage
import com.williamfq.domain.model.MessageType
import com.williamfq.xhat.domain.model.chat.ChatRoom
import com.williamfq.xhat.ui.chat.viewmodel.ChatMessagesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessagesScreen(
    roomId: String,
    viewModel: ChatMessagesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showEmojiPicker by remember { mutableStateOf(false) }

    LaunchedEffect(roomId) {
        viewModel.joinRoom(roomId)
    }

    Scaffold(
        topBar = {
            ChatMessagesTopBar(
                room = uiState.room,
                onBackClick = onNavigateUp,
                onInfoClick = { /* TODO: Mostrar información de la sala */ }
            )
        },
        bottomBar = {
            ChatMessageInput(
                message = uiState.currentMessage,
                onMessageChange = viewModel::updateCurrentMessage,
                onSendClick = viewModel::sendMessage,
                onAttachmentClick = { /* TODO: Implementar adjuntos */ },
                onEmojiClick = { showEmojiPicker = !showEmojiPicker }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uiState.messages.filterIsInstance<ChatMessage>(),
                    key = { message -> message.messageId }
                ) { message ->
                    ChatMessageItem(
                        message = message,
                        isCurrentUser = message.senderId == uiState.currentUserId
                    )
                }
            }

            AnimatedTypingIndicator(
                typingUsers = uiState.typingUsers,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            uiState.error?.let { error ->
                ErrorSnackbar(
                    message = error,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun AnimatedTypingIndicator(
    typingUsers: List<String>,
    modifier: Modifier = Modifier
) {
    if (typingUsers.isNotEmpty()) {
        Surface(
            modifier = modifier
                .padding(16.dp)
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = buildString {
                    append(typingUsers.joinToString(", "))
                    append(" está")
                    if (typingUsers.size > 1) append("n")
                    append(" escribiendo...")
                },
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp)
    ) {
        Text(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessagesTopBar(
    room: ChatRoom?,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    TopAppBar(
        title = {
            room?.let {
                Column {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${it.memberCount} miembros",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatMessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onEmojiClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onAttachmentClick) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Adjuntar"
                )
            }
            IconButton(onClick = onEmojiClick) {
                Icon(
                    Icons.Default.EmojiEmotions,
                    contentDescription = "Emojis"
                )
            }
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mensaje") },
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            IconButton(
                onClick = onSendClick,
                enabled = message.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar"
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if (isCurrentUser) "Tú" else message.username,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isCurrentUser) 16.dp else 0.dp,
                topEnd = if (isCurrentUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            color = if (isCurrentUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    MessageType.IMAGE -> {
                        Text("Imagen")
                    }
                    else -> {
                        Text("Tipo de mensaje no soportado")
                    }
                }
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
private fun ChatMessagesScreenPreview() {
    MaterialTheme {
        ChatMessagesScreen(
            roomId = "preview",
            onNavigateUp = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatMessageItemPreview() {
    MaterialTheme {
        Column {
            ChatMessageItem(
                message = ChatMessage(
                    id = 0,
                    messageId = "1",
                    chatId = "chat1",
                    senderId = "user1",
                    recipientId = "chat1",
                    content = "¡Hola! ¿Cómo estás?",
                    username = "Juan",
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.TEXT
                ),
                isCurrentUser = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChatMessageItem(
                message = ChatMessage(
                    id = 0,
                    messageId = "2",
                    chatId = "chat1",
                    senderId = "currentUser",
                    recipientId = "chat1",
                    content = "¡Muy bien! ¿Y tú?",
                    username = "Tú",
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.TEXT
                ),
                isCurrentUser = true
            )
        }
    }
}