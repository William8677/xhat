/*
 * Updated: 2025-02-08 03:21:28
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
// Importamos ChatMessage ya que ahora se utiliza ese tipo
import com.williamfq.domain.model.ChatMessage
import com.williamfq.xhat.ui.screens.chat.components.*
import com.williamfq.xhat.ui.screens.chat.model.ChatPreview
import com.williamfq.xhat.ui.screens.chat.viewmodel.ChatViewModel
import com.williamfq.xhat.ui.screens.chat.viewmodel.ChatViewModelInterface
import com.williamfq.xhat.ui.screens.chat.viewmodel.PreviewChatViewModel
import com.williamfq.xhat.ui.theme.XhatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatId: String? = null,
    viewModel: ChatViewModelInterface = hiltViewModel<ChatViewModel>(),
    isDetailView: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chatId) {
        chatId?.let { viewModel.loadChat(it) }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                title = if (isDetailView) uiState.chatTitle else "Chats",
                onBackClick = { navController.navigateUp() },
                isDetailView = isDetailView,
                onMenuClick = viewModel::onMenuClick,
                onCallClick = viewModel::onCallClick,
                onVideoCallClick = viewModel::onVideoCallClick,
                onMenuOptionSelected = viewModel::onMenuOptionSelected
            )
        },
        bottomBar = {
            if (isDetailView) {
                ChatBottomBar(
                    message = uiState.currentMessage,
                    onMessageChange = viewModel::onMessageChange,
                    onSendClick = viewModel::onSendMessage,
                    onAttachmentClick = viewModel::onAttachmentClick,
                    onWalkieTalkiePressed = viewModel::onWalkieTalkiePressed,
                    onWalkieTalkieReleased = viewModel::onWalkieTalkieReleased,
                    isWalkieTalkieActive = uiState.isWalkieTalkieActive
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                isDetailView -> {
                    MessagesList(
                        messages = uiState.messages, // List<ChatMessage>
                        onMessageClick = viewModel::onMessageClick,
                        onMessageLongPress = viewModel::onMessageLongPress
                    )
                }
                else -> {
                    ChatsList(
                        chats = uiState.chats,
                        onChatClick = { chatId: String ->
                            navController.navigate("chat_detail/$chatId")
                        }
                    )
                }
            }

            // Error Snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessage>,
    onMessageClick: (ChatMessage) -> Unit,
    onMessageLongPress: (ChatMessage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        reverseLayout = true
    ) {
        items(messages, key = { it.messageId }) { message ->
            ChatMessageItem(
                message = message,
                onClick = { onMessageClick(message) },
                onLongPress = { onMessageLongPress(message) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChatsList(
    chats: List<ChatPreview>,
    onChatClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(chats, key = { it.id }) { chat ->
            ChatItem(
                chat = chat,
                onClick = { onChatClick(chat.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    XhatTheme {
        ChatScreen(
            navController = rememberNavController(),
            chatId = "preview_chat",
            viewModel = PreviewChatViewModel(),
            isDetailView = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagesListPreview() {
    XhatTheme {
        MessagesList(
            messages = listOf(
                ChatMessage(
                    id = 0,
                    messageId = "1",
                    chatId = "preview",
                    content = "¡Hola! ¿Cómo estás?",
                    senderId = "user1",
                    recipientId = "user2",
                    timestamp = System.currentTimeMillis(),
                    username = "Juan"
                ),
                ChatMessage(
                    id = 0,
                    messageId = "2",
                    chatId = "preview",
                    content = "¡Bien! ¿Y tú?",
                    senderId = "user2",
                    recipientId = "user1",
                    timestamp = System.currentTimeMillis(),
                    username = "María"
                )
            ),
            onMessageClick = {},
            onMessageLongPress = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatsListPreview() {
    XhatTheme {
        ChatsList(
            chats = listOf(
                ChatPreview(
                    id = "1",
                    title = "Juan Pérez",
                    lastMessage = "¿Nos vemos mañana?",
                    timestamp = System.currentTimeMillis()
                ),
                ChatPreview(
                    id = "2",
                    title = "Grupo Familia",
                    lastMessage = "¡Feliz cumpleaños!",
                    timestamp = System.currentTimeMillis()
                )
            ),
            onChatClick = {}
        )
    }
}
