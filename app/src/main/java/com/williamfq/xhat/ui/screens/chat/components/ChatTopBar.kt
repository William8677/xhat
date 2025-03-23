/*
 * Updated: 2025-02-06 01:40:51
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.ui.screens.chat.model.ChatMenuOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    title: String,
    onBackClick: () -> Unit,
    isDetailView: Boolean,
    onMenuClick: () -> Unit = {}, // Agregar este parámetro con valor por defecto
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onMenuOptionSelected: (ChatMenuOption) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            if (isDetailView) {
                // Botón para llamada de voz
                IconButton(onClick = onCallClick) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Llamada de voz"
                    )
                }
                // Botón para videollamada
                IconButton(onClick = onVideoCallClick) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Videollamada"
                    )
                }
                // Botón de búsqueda
                IconButton(onClick = { onMenuOptionSelected(ChatMenuOption.Search) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                }
                // Botón de menú (más opciones) que despliega un menú flotante
                Box {
                    IconButton(
                        onClick = {
                            showMenu = true
                            onMenuClick() // Llamar a onMenuClick cuando se abre el menú
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                            onMenuClick() // Llamar a onMenuClick cuando se cierra el menú
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(end = 8.dp)
                    ) {
                        ChatMenuOption.getAllOptions().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.title) },
                                onClick = {
                                    onMenuOptionSelected(option)
                                    showMenu = false
                                    onMenuClick() // Llamar a onMenuClick al seleccionar una opción
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.title
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}