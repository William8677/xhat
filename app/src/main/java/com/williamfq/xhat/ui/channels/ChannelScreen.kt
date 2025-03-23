/*
 * Updated: 2025-01-25 22:50:23
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.ui.channels.components.ChannelHeader
import com.williamfq.xhat.ui.channels.components.ChannelPost as ChannelPostItem
import com.williamfq.xhat.ui.channels.components.CreatePostDialog
import com.williamfq.xhat.ui.channels.components.PostOptionsMenu
import com.williamfq.xhat.ui.channels.viewmodel.ChannelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelScreen(
    channelId: String,
    viewModel: ChannelViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val channel = uiState.channels.find { it.id == channelId }
    var showCreatePost by remember { mutableStateOf(false) }
    var showPostOptions by remember { mutableStateOf<String?>(null) }

    // Cargar las publicaciones para este canal
    LaunchedEffect(channelId) {
        viewModel.loadPosts(channelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(channel?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Compartir canal */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { /* TODO: Más opciones */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más")
                    }
                }
            )
        },
        floatingActionButton = {
            if (channel?.createdBy == viewModel.getCurrentUserId()) {
                FloatingActionButton(
                    onClick = { showCreatePost = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear publicación")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            channel?.let { ch ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cabecera del canal
                    item {
                        ChannelHeader(channel = ch)
                    }

                    // Publicaciones
                    items(uiState.posts) { post ->
                        ChannelPostItem(
                            post = post,
                            isAdmin = channel.createdBy == viewModel.getCurrentUserId(),
                            onReactionClick = { emoji ->
                                viewModel.addReactionToPost(channelId, post.id, emoji)
                            },
                            onShareClick = { /* TODO: Implementar compartir */ },
                            onOptionsClick = { showPostOptions = post.id }
                        )
                    }
                }

                // Diálogo para crear publicación
                if (showCreatePost) {
                    CreatePostDialog(
                        onDismiss = { showCreatePost = false },
                        onCreatePost = { content, type, attachments, poll ->
                            viewModel.createPost(
                                channelId = channelId,
                                content = content,
                                contentType = type,
                                attachments = attachments,
                                poll = poll
                            )
                            showCreatePost = false
                        }
                    )
                }

                // Menú de opciones de publicación
                showPostOptions?.let { postId ->
                    // Buscar el post seleccionado para extraer información (por ejemplo, isPinned)
                    val selectedPost = uiState.posts.find { it.id == postId }
                    selectedPost?.let { post ->
                        PostOptionsMenu(
                            onDismiss = { showPostOptions = null },
                            onDelete = {
                                viewModel.deletePost(channelId, postId)
                                showPostOptions = null
                            },
                            onPin = {
                                viewModel.pinPost(channelId, postId)
                                showPostOptions = null
                            },
                            isAdmin = channel.createdBy == viewModel.getCurrentUserId(),
                            isPinned = post.isPinned
                        )
                    }
                }
            }

            // Snackbar para errores
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}
