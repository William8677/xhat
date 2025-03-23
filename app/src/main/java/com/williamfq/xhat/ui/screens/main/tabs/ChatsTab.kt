/*
 * Updated: 2025-02-05 05:30:00
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.main.tabs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.williamfq.xhat.ui.screens.chat.ChatScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.ui.screens.chat.viewmodel.ChatViewModel

@Composable
fun ChatsTab(navController: NavHostController) {
    val viewModel: ChatViewModel = hiltViewModel()
    ChatScreen(
        navController = navController,
        chatId = "",  // Puedes ajustar este valor según la lógica de tu aplicación
        viewModel = viewModel,
        isDetailView = false
    )
}
