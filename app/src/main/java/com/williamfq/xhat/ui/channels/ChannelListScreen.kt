/*
 * Updated: 2025-01-22 01:43:04
 * Author: William8677
 */

package com.williamfq.xhat.ui.channels

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.domain.model.ChannelCategory
import com.williamfq.xhat.ui.channels.components.*
import com.williamfq.xhat.ui.channels.viewmodel.ChannelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    viewModel: ChannelViewModel = hiltViewModel(),
    onNavigateToChannel: (String) -> Unit,
    onNavigateToCreateChannel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canales") },
                actions = {
                    // Botón de búsqueda
                    IconButton(onClick = { /* TODO: Implementar búsqueda */ }) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                    // Botón de filtros
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filtros")
                    }
                    // Botón de crear canal
                    IconButton(onClick = onNavigateToCreateChannel) {
                        Icon(Icons.Default.Add, "Crear canal")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Canales verificados/oficiales
                    item {
                        Text(
                            text = "Canales destacados",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    val verifiedChannels = uiState.channels.filter { it.isVerified }
                    items(verifiedChannels) { channel ->
                        VerifiedChannelCard(
                            channel = channel,
                            onClick = { onNavigateToChannel(channel.id) },
                            onSubscribe = { viewModel.subscribeToChannel(channel.id) }
                        )
                    }

                    // Canales por categoría
                    ChannelCategory.values().forEach { category ->
                        val channelsInCategory = uiState.channels.filter {
                            it.category == category && !it.isVerified
                        }

                        if (channelsInCategory.isNotEmpty()) {
                            item {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }

                            items(channelsInCategory) { channel ->
                                ChannelCard(
                                    channel = channel,
                                    onClick = { onNavigateToChannel(channel.id) },
                                    onSubscribe = { viewModel.subscribeToChannel(channel.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Error Snackbar
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

        // Hoja de filtros
        if (showFilterSheet) {
            ChannelFilterSheet(
                currentFilter = uiState.filter,
                onFilterChange = viewModel::updateFilter,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}