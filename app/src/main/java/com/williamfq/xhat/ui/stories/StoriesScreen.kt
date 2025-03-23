package com.williamfq.xhat.ui.stories

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.android.gms.ads.nativead.NativeAd
import com.williamfq.domain.model.*
import com.williamfq.domain.repository.StoryRepository
import com.williamfq.domain.repository.UserRepository
import com.williamfq.xhat.ads.ui.components.NativeStoryAd
import com.williamfq.xhat.ui.stories.components.StoryPoll
import com.williamfq.xhat.ui.stories.components.StoryQuiz
import com.williamfq.xhat.ui.stories.components.StoryViewer
import com.williamfq.xhat.ui.stories.viewmodel.StoriesViewModel
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.metrics.EngagementType
import com.williamfq.xhat.utils.metrics.StoryMetricsReport
import com.williamfq.xhat.utils.metrics.StoryMetricsTracker
import com.williamfq.xhat.utils.share.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@Composable
private fun StoryCard(
    story: Story,
    onLongPress: () -> Unit,
    onPress: (Offset) -> Unit,
    onPressRelease: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onPress = { offset ->
                        onPress(offset)
                        try {
                            tryAwaitRelease()
                        } catch (e: Exception) {
                            Timber.e(e, "Error en gesture release para storyId=${story.id}")
                        }
                        onPressRelease()
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (story.mediaType) {
                MediaType.IMAGE, MediaType.VIDEO -> {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(story.backgroundColor ?: MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = story.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = story.textColor ?: MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun StoriesScreen(
    navController: NavHostController,
    viewModel: StoriesViewModel = hiltViewModel(),
    onNavigateToAddStory: () -> Unit = {}
) {
    val stories by viewModel.stories.collectAsState()
    val selectedStory by viewModel.selectedStory.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    val showingAd by viewModel.showingAd.collectAsState()
    val nativeAd by viewModel.nativeStoryAdManager.currentNativeAd.collectAsState()
    var previewStory by remember { mutableStateOf<Story?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadStories()
            Timber.d("Cargando historias en StoriesScreen")
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar historias en StoriesScreen")
            scope.launch {
                snackbarHostState.showSnackbar("Error al cargar historias: ${e.message}")
            }
        }
    }

    LaunchedEffect(uiEvent) {
        when (uiEvent) {
            is StoriesViewModel.UiEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar((uiEvent as StoriesViewModel.UiEvent.ShowSnackbar).message)
                Timber.d("Mostrando snackbar: ${(uiEvent as StoriesViewModel.UiEvent.ShowSnackbar).message}")
            }

            is StoriesViewModel.UiEvent.NavigateToUserSelection -> {
                navController.navigate("user_selection")
                Timber.d("Navegando a selección de usuarios")
            }

            is StoriesViewModel.UiEvent.ShowAd -> {
                Timber.d("Mostrando anuncio en StoriesScreen")
            }

            is StoriesViewModel.UiEvent.ShowEffect -> {
                Timber.d("Aplicando efecto en StoriesScreen")
            }

            null -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { showOptionsMenu = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.MoreVert, "Más opciones")
                }
                FloatingActionButton(
                    onClick = {
                        try {
                            onNavigateToAddStory()
                            Timber.d("Navegando a agregar historia")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a agregar historia")
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar: ${e.message}")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "Agregar historia")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (stories.isEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay historias disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stories) { story ->
                        StoryCard(
                            story = story,
                            onLongPress = {
                                previewStory = story
                                showPreview = true
                                try {
                                    viewModel.addInteraction(
                                        story.id,
                                        StoryInteraction(
                                            userId = currentUserId ?: return@StoryCard,
                                            type = InteractionType.SAVE,
                                            content = "Vista previa de historia guardada",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    Timber.d("Historia guardada en vista previa: ${story.id}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al guardar interacción de vista previa: storyId=${story.id}")
                                }
                            },
                            onPress = {
                                try {
                                    viewModel.selectStory(story)
                                    viewModel.addInteraction(
                                        story.id,
                                        StoryInteraction(
                                            userId = currentUserId ?: return@StoryCard,
                                            type = InteractionType.VIEW,
                                            content = "Historia vista",
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    Timber.d("Historia seleccionada y vista: ${story.id}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al seleccionar historia: storyId=${story.id}")
                                }
                            },
                            onPressRelease = {
                                scope.launch {
                                    delay(100)
                                    if (previewStory == story && !showPreview) {
                                        previewStory = null
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showOptionsMenu) {
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Crear grupo de historias") },
                        onClick = {
                            try {
                                val storyId = selectedStory?.id ?: throw IllegalStateException("No hay historia seleccionada")
                                val userId = currentUserId ?: throw IllegalStateException("Usuario no identificado")
                                viewModel.addInteraction(
                                    storyId,
                                    StoryInteraction(
                                        userId = userId,
                                        type = InteractionType.CUSTOM_ACTION,
                                        content = "Grupo de historias creado",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                Timber.d("Grupo de historias creado para: $storyId")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al crear grupo de historias")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                            showOptionsMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Group, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Reportar historia") },
                        onClick = {
                            try {
                                val storyId = selectedStory?.id ?: throw IllegalStateException("No hay historia seleccionada")
                                val userId = currentUserId ?: throw IllegalStateException("Usuario no identificado")
                                viewModel.addInteraction(
                                    storyId,
                                    StoryInteraction(
                                        userId = userId,
                                        type = InteractionType.REPORT,
                                        content = "Historia reportada",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                Timber.d("Historia reportada: $storyId")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al reportar historia")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                            showOptionsMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Warning, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Configuración") },
                        onClick = {
                            try {
                                navController.navigate("settings")
                                Timber.d("Navegando a configuración")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a configuración")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                            showOptionsMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, null) }
                    )
                }
            }

            AnimatedVisibility(
                visible = showPreview && previewStory != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Dialog(
                    onDismissRequest = { showPreview = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    previewStory?.let { story ->
                        StoryPreviewContent(story, navController, viewModel)
                    }
                }
            }

            when (uiState) {
                is StoriesViewModel.UiState.ViewingStory -> {
                    selectedStory?.let { story ->
                        StoryViewer(
                            story = story,
                            onDismiss = {
                                try {
                                    viewModel.closeStory()
                                    Timber.d("Cerrando visor de historia: ${story.id}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al cerrar visor de historia: storyId=${story.id}")
                                }
                            },
                            onReaction = { reaction ->
                                val reactionType = mapStoryReactionToType(reaction)
                                try {
                                    viewModel.handleReaction(
                                        storyId = story.id,
                                        reaction = Reaction(
                                            id = UUID.randomUUID().toString(),
                                            userId = currentUserId ?: return@StoryViewer,
                                            type = reactionType,
                                            content = reaction.description,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    Timber.d("Reacción añadida a historia: ${story.id}, tipo: $reactionType")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al añadir reacción: storyId=${story.id}")
                                }
                            },
                            onComment = { commentText ->
                                try {
                                    viewModel.handleComment(storyId = story.id, commentText = commentText)
                                    Timber.d("Comentario añadido a historia: ${story.id}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al añadir comentario: storyId=${story.id}")
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                }
                is StoriesViewModel.UiState.SelectUsers -> {}
                is StoriesViewModel.UiState.Initial -> {}
            }

            if (showingAd && nativeAd != null) {
                NativeStoryAd(
                    nativeAd = nativeAd as NativeAd,
                    onAdComplete = {
                        try {
                            viewModel.closeStory()
                            val storyId = selectedStory?.id ?: return@NativeStoryAd
                            val userId = currentUserId ?: return@NativeStoryAd
                            viewModel.addInteraction(
                                storyId,
                                StoryInteraction(
                                    userId = userId,
                                    type = InteractionType.LINK_CLICK,
                                    content = "Anuncio completado",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            Timber.d("Anuncio completado para historia: $storyId")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al completar anuncio")
                        }
                    },
                    onAdSkipped = {
                        try {
                            viewModel.closeStory()
                            val storyId = selectedStory?.id ?: return@NativeStoryAd
                            val userId = currentUserId ?: return@NativeStoryAd
                            viewModel.addInteraction(
                                storyId,
                                StoryInteraction(
                                    userId = userId,
                                    type = InteractionType.SWIPE_UP,
                                    content = "Anuncio omitido",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            Timber.d("Anuncio omitido para historia: $storyId")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al omitir anuncio")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StoryPreviewContent(
    story: Story,
    navController: NavHostController,
    viewModel: StoriesViewModel
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(story.backgroundColor ?: Color.Black)
    ) {
        when (story.mediaType) {
            MediaType.IMAGE -> StoryImageContent(story)
            MediaType.VIDEO -> StoryVideoContent(story)
            MediaType.TEXT -> StoryTextContent(story)
            MediaType.AUDIO -> StoryAudioContent(story)
            MediaType.AR -> StoryARContent(story)
            MediaType.DOCUMENT -> StoryDocumentContent(story)
            MediaType.LOCATION -> StoryLocationContent(story)
            MediaType.CONTACT -> StoryContactContent(story)
            MediaType.POLL -> StoryPoll(story, viewModel)
            MediaType.QUIZ -> StoryQuiz(story, viewModel)
            MediaType.LINK -> StoryLinkContent(story)
            MediaType.CUSTOM -> StoryCustomContent(story)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = story.description,
                style = MaterialTheme.typography.bodyMedium,
                color = story.textColor ?: Color.White
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        try {
                            viewModel.addInteraction(
                                story.id,
                                StoryInteraction(
                                    userId = currentUserId ?: return@IconButton,
                                    type = InteractionType.SHARE,
                                    content = "Historia compartida",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            Timber.d("Historia compartida: ${story.id}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al compartir historia: storyId=${story.id}")
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                }
                IconButton(
                    onClick = {
                        try {
                            viewModel.addInteraction(
                                story.id,
                                StoryInteraction(
                                    userId = currentUserId ?: return@IconButton,
                                    type = InteractionType.REPLY,
                                    content = "Respondido a historia",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            Timber.d("Respondido a historia: ${story.id}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al responder a historia: storyId=${story.id}")
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Reply, "Responder", tint = Color.White)
                }
                IconButton(onClick = {
                    try {
                        navController.popBackStack()
                        Timber.d("Cerrando vista previa de historia: ${story.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al cerrar vista previa: storyId=${story.id}")
                    }
                }) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                }
            }
        }
        story.interactiveElements?.forEach { element ->
            Button(
                onClick = {
                    try {
                        element.action?.invoke()
                        Timber.d("Acción interactiva ejecutada en historia: ${story.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al ejecutar acción interactiva: storyId=${story.id}")
                    }
                },
                modifier = Modifier
                    .offset(x = element.position.x.dp, y = element.position.y.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = element.icon, contentDescription = element.title)
                Text(text = element.actionLabel ?: element.title)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            story.mentions.forEach { mention ->
                Text(
                    text = when (mention.mentionType) {
                        MentionType.USER -> "@${mention.username}"
                        MentionType.GROUP -> "Grupo: ${mention.username}"
                        MentionType.CHANNEL -> "Canal: ${mention.username}"
                        MentionType.LOCATION -> "Ubicación: ${mention.username}"
                        MentionType.PRODUCT -> "Producto: ${mention.username}"
                        MentionType.EVENT -> "Evento: ${mention.username}"
                    },
                    color = story.textColor ?: Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            story.hashtags.forEach { hashtag ->
                Text(
                    text = "#${hashtag.tag}",
                    color = story.textColor ?: Color.Cyan,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StoryImageContent(story: Story) {
    var isLoading by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        story.mediaUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = story.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onLoading = { isLoading = true },
                onSuccess = {
                    isLoading = false
                    Timber.d("Imagen cargada exitosamente: ${story.mediaUrl}, storyId=${story.id}")
                },
                onError = {
                    isLoading = false
                    Timber.e("Error al cargar imagen: ${story.mediaUrl}, storyId=${story.id}")
                }
            )
        } ?: Text(
            text = "Imagen no disponible",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        StoryOverlay(story)
    }
}

@Composable
private fun StoryVideoContent(story: Story) {
    var isLoading by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        story.mediaUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = story.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onLoading = { isLoading = true },
                onSuccess = {
                    isLoading = false
                    Timber.d("Video cargado exitosamente: ${story.mediaUrl}, storyId=${story.id}")
                },
                onError = {
                    isLoading = false
                    Timber.e("Error al cargar video: ${story.mediaUrl}, storyId=${story.id}")
                }
            )
        } ?: Text(
            text = "Video no disponible",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        StoryOverlay(story)
    }
}

@Composable
private fun StoryTextContent(story: Story) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = story.description,
            style = MaterialTheme.typography.bodyLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryAudioContent(story: Story) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = "Audio",
            modifier = Modifier.size(64.dp),
            tint = story.textColor ?: Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = story.title,
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryARContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = story.arInstructions ?: "Contenido AR no disponible",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryDocumentContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Documento: ${story.title}",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryLocationContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ubicación: ${story.content}",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryContactContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Contacto: ${story.content}",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryLinkContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Enlace: ${story.mediaUrl ?: "Sin URL"}",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryCustomContent(story: Story) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Contenido personalizado: ${story.content}",
            style = MaterialTheme.typography.titleLarge,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StoryOverlay(story: Story) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = story.title,
                style = MaterialTheme.typography.titleMedium,
                color = story.textColor ?: Color.White
            )
            Text(
                text = getTimeAgo(story.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = (story.textColor ?: Color.White).copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            story.reactions.forEach { reaction ->
                when (reaction.type) {
                    ReactionType.LIKE -> Icon(Icons.Default.ThumbUp, "Like", tint = Color.White, modifier = Modifier.size(20.dp))
                    ReactionType.LOVE -> Icon(Icons.Default.Favorite, "Love", tint = Color.Red, modifier = Modifier.size(20.dp))
                    ReactionType.HAHA -> Text("😂", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.WOW -> Text("😮", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.SAD -> Text("😢", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.ANGRY -> Text("😡", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CARE -> Text("🤗", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.SUPPORT -> Text("👍", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CELEBRATE -> Text("🎉", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CURIOUS -> Text("🤔", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.THOUGHTFUL -> Text("💭", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CUSTOM -> Text(reaction.content, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.FIRE -> Text("🔥", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CLAP -> Text("👏", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.SMILE -> Text("😊", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.LAUGH -> Text("🤣", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.CRY -> Text("😭", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.THINK -> Text("🤓", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.EYES -> Text("👀", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.HUNDRED -> Text("💯", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.HEART_EYES -> Text("😍", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    ReactionType.MINDBLOWN -> Text("🤯", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun mapStoryReactionToType(reaction: StoryReaction): ReactionType {
    return when (reaction) {
        StoryReaction.LIKE -> ReactionType.LIKE
        StoryReaction.LOVE -> ReactionType.LOVE
        StoryReaction.LAUGH -> ReactionType.LAUGH
        StoryReaction.WOW -> ReactionType.WOW
        StoryReaction.SAD -> ReactionType.SAD
        StoryReaction.ANGRY -> ReactionType.ANGRY
        StoryReaction.SUPPORT -> ReactionType.SUPPORT
        StoryReaction.CELEBRATE -> ReactionType.CELEBRATE
        StoryReaction.FIRE -> ReactionType.FIRE
        StoryReaction.CLAP -> ReactionType.CLAP
        StoryReaction.THINK -> ReactionType.THINK
        StoryReaction.EYES -> ReactionType.EYES
        StoryReaction.HUNDRED -> ReactionType.HUNDRED
        StoryReaction.HEART_EYES -> ReactionType.HEART_EYES
        StoryReaction.MINDBLOWN -> ReactionType.MINDBLOWN
    }
}

private fun getTimeAgo(timestamp: Long): String {
    return try {
        val now = LocalDateTime.now()
        val storyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        val minutes = ChronoUnit.MINUTES.between(storyTime, now)
        when {
            minutes < 60 -> "$minutes min"
            minutes < 1440 -> "${minutes / 60}h"
            minutes < 10080 -> "${minutes / 1440}d"
            else -> "${minutes / 10080}w"
        }
    } catch (e: Exception) {
        Timber.e(e, "Error calculando tiempo transcurrido")
        "Desconocido"
    }
}

