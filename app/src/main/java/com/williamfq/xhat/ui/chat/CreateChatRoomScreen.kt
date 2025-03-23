/*
 * Updated: 2025-01-25 01:54:23
 * Author: William8677
 */
package com.williamfq.xhat.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.domain.model.chat.ChatRoomCategory
import com.williamfq.xhat.ui.chat.viewmodel.CreateChatRoomUiState
import com.williamfq.xhat.ui.chat.viewmodel.CreateChatRoomViewModel
import com.williamfq.xhat.R
// Importa la propiedad de extensión definida en ChatExtensions.kt
import com.williamfq.xhat.ui.chat.title

// Propiedad de extensión para el estado de la UI que indica si es válido (nombre y descripción no vacíos)
val CreateChatRoomUiState.isValid: Boolean
    get() = name.isNotBlank() && description.isNotBlank()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatRoomScreen(
    viewModel: CreateChatRoomViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear sala de chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo para el nombre de la sala
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nombre de la sala") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = if (uiState.nameError != null) {
                    { Text(uiState.nameError!!) }
                } else null
            )

            // Campo para la descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = uiState.descriptionError != null,
                supportingText = if (uiState.descriptionError != null) {
                    { Text(uiState.descriptionError!!) }
                } else null
            )

            // Categoría (usando un menú desplegable)
            ExposedDropdownMenuBox(
                expanded = uiState.showCategoryMenu,
                onExpandedChange = { viewModel.toggleCategoryMenu(it) }
            ) {
                OutlinedTextField(
                    value = uiState.category?.title ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showCategoryMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = uiState.showCategoryMenu,
                    onDismissRequest = { viewModel.toggleCategoryMenu(false) }
                ) {
                    ChatRoomCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.title) },
                            onClick = {
                                viewModel.updateCategory(category)
                                viewModel.toggleCategoryMenu(false)
                            }
                        )
                    }
                }
            }

            // Opciones avanzadas
            var showAdvancedOptions by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showAdvancedOptions = !showAdvancedOptions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showAdvancedOptions) "Ocultar opciones avanzadas" else "Mostrar opciones avanzadas")
            }

            if (showAdvancedOptions) {
                // Campo para límite de usuarios (opcional)
                OutlinedTextField(
                    value = uiState.maxUsers?.toString() ?: "",
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { viewModel.updateMaxUsers(it) }
                    },
                    label = { Text("Límite de usuarios (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Switch para sala privada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sala privada")
                    Switch(
                        checked = uiState.isPrivate,
                        onCheckedChange = { viewModel.updateIsPrivate(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de crear sala
            Button(
                onClick = viewModel::createChatRoom,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear sala")
                }
            }
        }
    }

    // Mostrar errores, si existen
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un Snackbar o realizar otra acción para notificar el error
        }
    }

    // Navegar si la creación fue exitosa
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateUp()
        }
    }
}
