/*
 * Updated: 2025-01-21 20:37:16
 * Author: William8677
 */

package com.williamfq.xhat.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.williamfq.xhat.domain.model.GalleryImage

@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GalleryTopBar(
                selectedCount = uiState.selectedImages.size,
                onNavigateBack = onNavigateBack,
                onShareClick = { viewModel.processAndShareImages(uiState.selectedImages) }, // Cambio aquí
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                LoadingScreen()
            } else if (uiState.images.isEmpty()) {
                EmptyGalleryScreen()
            } else {
                GalleryGrid(
                    images = uiState.images,
                    selectedImages = uiState.selectedImages,
                    onImageSelected = viewModel::selectImage
                )
            }

            uiState.error?.let { error ->
                ErrorSnackbar(
                    message = error,
                    onDismiss = viewModel::clearError
                )
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    count = uiState.selectedImages.size,
                    onConfirm = {
                        viewModel.deleteSelectedImages()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTopBar(
    selectedCount: Int,
    onNavigateBack: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (selectedCount > 0) {
                Text("$selectedCount seleccionados")
            } else {
                Text("Galería")
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            if (selectedCount > 0) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    )
}

@Composable
private fun GalleryGrid(
    images: List<GalleryImage>,
    selectedImages: List<GalleryImage>,
    onImageSelected: (GalleryImage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = images,
            key = { it.id }
        ) { image ->
            GalleryItem(
                image = image,
                isSelected = selectedImages.contains(image),
                onSelected = { onImageSelected(image) }
            )
        }
    }
}

@Composable
private fun GalleryItem(
    image: GalleryImage,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onSelected)
    ) {
        AsyncImage(
            model = image.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.small)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyGalleryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay imágenes",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    ) {
        Text(message)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar $count ${if (count == 1) "imagen" else "imágenes"}?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("ELIMINAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}