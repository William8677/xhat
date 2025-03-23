/*
 * Updated: 2025-01-26 18:30:26
 * Author: William8677
 */

package com.williamfq.xhat.ui.communities.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.ui.communities.components.CommunityRulesDialog
import com.williamfq.xhat.ui.communities.components.CommunityTagsDialog
import com.williamfq.xhat.ui.communities.viewmodel.CommunityType
import com.williamfq.xhat.ui.communities.viewmodel.CreateCommunityViewModel
import com.williamfq.xhat.ui.components.ImagePicker

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateCommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRulesDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Comunidad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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
            // Banner y avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de banner
                ImagePicker(
                    imageUrl = uiState.bannerUrl,
                    onImageSelected = viewModel::setBannerImage,
                    modifier = Modifier
                        .weight(2f)
                        .height(120.dp)
                )

                // Selector de avatar
                ImagePicker(
                    imageUrl = uiState.avatarUrl,
                    onImageSelected = viewModel::setAvatarImage,
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                )
            }

            // Nombre de la comunidad
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::setName,
                label = { Text("Nombre de la comunidad") },
                modifier = Modifier.fillMaxWidth()
            )

            // Descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Tipo de comunidad
            CommunityTypeSelector(
                selectedType = uiState.type,
                onTypeSelected = viewModel::setType
            )

            // Reglas
            Card(
                onClick = { showRulesDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reglas de la comunidad",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${uiState.rules.size} reglas establecidas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Tags
            Card(
                onClick = { showTagsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Etiquetas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            // Configuración adicional
            CommunitySettings(
                isNSFW = uiState.isNSFW,
                onNSFWChange = viewModel::setNSFW,
                isPrivate = uiState.isPrivate,
                onPrivateChange = viewModel::setPrivate,
                allowCrossPosts = uiState.allowCrossPosts,
                onCrossPostsChange = viewModel::setAllowCrossPosts
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón crear
            Button(
                onClick = {
                    viewModel.createCommunity {
                        onNavigateBack()
                    }
                },
                enabled = uiState.isValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Comunidad")
                }
            }
        }
    }

    // Diálogos
    if (showRulesDialog) {
        CommunityRulesDialog(
            rules = uiState.rules,
            onRulesUpdated = viewModel::setRules,
            onDismiss = { showRulesDialog = false }
        )
    }

    if (showTagsDialog) {
        CommunityTagsDialog(
            tags = uiState.tags,
            onTagsUpdated = viewModel::setTags,
            onDismiss = { showTagsDialog = false }
        )
    }
}

@Composable
private fun CommunityTypeSelector(
    selectedType: CommunityType,
    onTypeSelected: (CommunityType) -> Unit
) {
    Column {
        Text(
            text = "Tipo de comunidad",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CommunityType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioButton(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) }
                )
                Column {
                    Text(
                        text = type.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = type.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunitySettings(
    isNSFW: Boolean,
    onNSFWChange: (Boolean) -> Unit,
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    allowCrossPosts: Boolean,
    onCrossPostsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Contenido NSFW")
            Switch(
                checked = isNSFW,
                onCheckedChange = onNSFWChange
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Comunidad privada")
            Switch(
                checked = isPrivate,
                onCheckedChange = onPrivateChange
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Permitir cross-posts")
            Switch(
                checked = allowCrossPosts,
                onCheckedChange = onCrossPostsChange
            )
        }
    }
}