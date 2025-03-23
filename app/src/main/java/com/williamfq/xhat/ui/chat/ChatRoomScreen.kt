package com.williamfq.xhat.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.domain.model.chat.ChatRoom
import com.williamfq.xhat.domain.model.chat.ChatRoomCategory
import com.williamfq.xhat.domain.model.chat.ChatRoomType
import com.williamfq.xhat.ui.chat.viewmodel.ChatRoomViewModel
import java.text.SimpleDateFormat
import java.util.*

// Se elimina la definición de extensión privada de "title" ya que ahora se usa la definida en ChatExtensions.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: ChatRoomViewModel = hiltViewModel(),
    onNavigateToCreateRoom: () -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ChatTopBar(
                onFilterClick = { showFilterSheet = true },
                onCreateRoomClick = onNavigateToCreateRoom
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pestañas de categorías de salas
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedCategoryIndex,
                edgePadding = 16.dp
            ) {
                ChatRoomCategory.values().forEachIndexed { index, category ->
                    Tab(
                        selected = index == uiState.selectedCategoryIndex,
                        onClick = { viewModel.selectCategory(index) },
                        text = { Text(category.title) }
                    )
                }
            }

            // Lista de salas
            ChatRoomList(
                rooms = uiState.filteredRooms,
                onRoomClick = viewModel::selectRoom,
                onJoinClick = viewModel::joinRoom
            )
        }
    }

    // Hoja de filtros
    if (showFilterSheet) {
        ChatFilterSheet(
            currentFilter = uiState.filter,
            onFilterChange = viewModel::updateFilter,
            onDismiss = { showFilterSheet = false }
        )
    }

    // Diálogo de sala seleccionada
    uiState.selectedRoom?.let { room ->
        ChatRoomDialog(
            room = room,
            onDismiss = viewModel::clearSelectedRoom,
            onJoin = {
                viewModel.joinRoom(room.id)
                viewModel.clearSelectedRoom()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    onFilterClick: () -> Unit,
    onCreateRoomClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Salas de Chat") },
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
            }
            IconButton(onClick = onCreateRoomClick) {
                Icon(Icons.Default.Add, contentDescription = "Crear sala")
            }
        }
    )
}

@Composable
private fun ChatRoomList(
    rooms: List<ChatRoom>,
    onRoomClick: (ChatRoom) -> Unit,
    onJoinClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rooms) { room ->
            ChatRoomCard(
                room = room,
                onClick = { onRoomClick(room) },
                onJoinClick = { onJoinClick(room.id) }
            )
        }
    }
}

@Composable
private fun ChatRoomCard(
    room: ChatRoom,
    onClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = room.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Unirse")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tipo de sala
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            when (room.type) {
                                ChatRoomType.SYSTEM_LOCATION -> "Ubicación"
                                ChatRoomType.SYSTEM_LANGUAGE -> "Idioma"
                                ChatRoomType.SYSTEM_FRIENDSHIP -> "Amistad"
                                ChatRoomType.USER_CREATED -> "Usuario"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (room.type) {
                                ChatRoomType.SYSTEM_LOCATION -> Icons.Default.LocationOn
                                ChatRoomType.SYSTEM_LANGUAGE -> Icons.Default.Language
                                ChatRoomType.SYSTEM_FRIENDSHIP -> Icons.Default.People
                                ChatRoomType.USER_CREATED -> Icons.Default.Person
                            },
                            contentDescription = null
                        )
                    }
                )
                // Miembros activos
                Text(
                    text = "${room.memberCount} miembros",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatFilterSheet(
    currentFilter: com.williamfq.xhat.data.repository.ChatRoomFilter,
    onFilterChange: (com.williamfq.xhat.data.repository.ChatRoomFilter) -> Unit,
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
            // Tipo de sala
            Text(
                text = "Tipo de sala",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ChatRoomType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = currentFilter.type == type,
                        onClick = { onFilterChange(currentFilter.copy(type = type)) }
                    )
                    Text(
                        text = when (type) {
                            ChatRoomType.SYSTEM_LOCATION -> "Ubicación"
                            ChatRoomType.SYSTEM_LANGUAGE -> "Idioma"
                            ChatRoomType.SYSTEM_FRIENDSHIP -> "Amistad"
                            ChatRoomType.USER_CREATED -> "Usuario"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            // Filtros de ubicación
            OutlinedTextField(
                value = currentFilter.country ?: "",
                onValueChange = {
                    onFilterChange(currentFilter.copy(country = it.ifEmpty { null }))
                },
                label = { Text("País") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentFilter.city ?: "",
                onValueChange = {
                    onFilterChange(currentFilter.copy(city = it.ifEmpty { null }))
                },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Idioma
            OutlinedTextField(
                value = currentFilter.language ?: "",
                onValueChange = {
                    onFilterChange(currentFilter.copy(language = it.ifEmpty { null }))
                },
                label = { Text("Idioma") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onFilterChange(com.williamfq.xhat.data.repository.ChatRoomFilter()) },
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

@Composable
private fun ChatRoomDialog(
    room: ChatRoom,
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(room.name) },
        text = {
            Column {
                Text(room.description)
                if (room.rules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reglas:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    room.rules.forEach { rule ->
                        Text("• $rule")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onJoin) {
                Text("Unirse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
