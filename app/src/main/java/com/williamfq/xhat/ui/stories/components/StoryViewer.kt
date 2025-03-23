package com.williamfq.xhat.ui.stories.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.graphicsLayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.williamfq.domain.model.*
import com.williamfq.xhat.ui.stories.StoryReaction
import com.williamfq.xhat.ui.stories.viewmodel.StoriesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

data class StoryUnsupportedContent(val message: String = "Tipo de contenido no soportado")

data class StoryInteractiveElement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val position: Offset,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)

sealed class StoryEffect {
    data class Sparkle(val position: Offset) : StoryEffect()
    data class Heart(val position: Offset) : StoryEffect()
    data class Star(val position: Offset) : StoryEffect()
}

data class Effect(
    val position: Offset,
    val type: EffectType,
    val createdAt: Long = System.currentTimeMillis()
)

enum class EffectType { SPARKLE, HEART, STAR }

@Composable
private fun AnimatedReactionPanel(
    visible: Boolean,
    onReaction: (StoryReaction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(StoryReaction.values()) { reaction ->
                    IconButton(onClick = { onReaction(reaction) }) {
                        Icon(
                            imageVector = reaction.icon,
                            contentDescription = reaction.description,
                            tint = reaction.color
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryViewer(
    story: Story,
    onDismiss: () -> Unit,
    onReaction: (StoryReaction) -> Unit,
    onComment: (String) -> Unit,
    viewModel: StoriesViewModel,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var showReactions by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var isDoubleTapped by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(story, isPaused) {
        if (!isPaused) {
            progress = 0f
            val totalDuration = (story.durationSeconds ?: 5) * 1000L
            val steps = 100
            val stepDuration = totalDuration / steps
            for (i in 0..steps) {
                if (!isPaused) {
                    progress = i.toFloat() / steps
                    delay(stepDuration)
                } else break
            }
            if (!isPaused && !isDoubleTapped) {
                try {
                    onDismiss()
                    Timber.d("StoryViewer: Historia cerrada: storyId=${story.id}")
                } catch (e: Exception) {
                    Timber.e(e, "Error al cerrar StoryViewer: storyId=${story.id}")
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    rotation += rotationChange
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300) {
                            isDoubleTapped = true
                            scope.launch {
                                try {
                                    onReaction(StoryReaction.LIKE)
                                    Timber.d("StoryViewer: Doble tap registrado como LIKE: storyId=${story.id}")
                                    delay(300)
                                    isDoubleTapped = false
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al registrar reacción LIKE: storyId=${story.id}")
                                }
                            }
                        }
                        lastTapTime = currentTime
                    },
                    onLongPress = { showReactions = true }
                )
            }
            .graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = rotation }
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .animateContentSize(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Box(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
            when (story.mediaType) {
                MediaType.IMAGE -> StoryImage(story)
                MediaType.VIDEO -> StoryVideo(story, { progress = it }, isPaused)
                MediaType.TEXT -> StoryText(story)
                MediaType.AUDIO -> StoryAudio(story, { progress = it }, isPaused)
                MediaType.AR -> StoryAR(story)
                MediaType.POLL -> StoryPoll(story, viewModel)
                MediaType.QUIZ -> StoryQuiz(story, viewModel)
                else -> StoryUnsupported()
            }
        }

        AnimatedReactionPanel(
            visible = showReactions,
            onReaction = { reaction ->
                scope.launch {
                    try {
                        onReaction(reaction)
                        showReactions = false
                        Timber.d("StoryViewer: Reacción seleccionada: ${reaction.name}, storyId=${story.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al procesar reacción en panel: storyId=${story.id}")
                    }
                }
            }
        )

        if (story.hasEffects) {
            StoryEffectsOverlay(story)
        }
        if (story.hasInteractiveElements) {
            StoryInteractiveElements(story)
        }

        AnimatedVisibility(
            visible = isDoubleTapped,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
private fun StoryImage(story: Story) {
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
                    Timber.d("StoryImage: Imagen cargada exitosamente: ${story.mediaUrl}, storyId=${story.id}")
                },
                onError = {
                    isLoading = false
                    Timber.e("StoryImage: Error al cargar imagen: ${story.mediaUrl}, storyId=${story.id}")
                }
            )
        } ?: run {
            Timber.w("StoryImage: No hay URL de imagen para storyId=${story.id}")
            Text(
                text = "Imagen no disponible",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StoryVideo(story: Story, onProgressUpdate: (Float) -> Unit, isPaused: Boolean) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(story.mediaUrl ?: return@apply))
            prepare()
            playWhenReady = !isPaused
            addListener(object : Player.Listener {
                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    if (duration > 0) onProgressUpdate(currentPosition.toFloat() / duration.toFloat())
                }
                override fun onPlayerError(error: PlaybackException) {
                    Timber.e(error, "StoryVideo: Error al reproducir video: storyId=${story.id}")
                }
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> Timber.d("StoryVideo: Video listo para reproducir: storyId=${story.id}")
                        Player.STATE_ENDED -> Timber.d("StoryVideo: Video terminado: storyId=${story.id}")
                    }
                }
            })
        }
    }

    DisposableEffect(isPaused) {
        if (isPaused) exoPlayer.pause() else exoPlayer.play()
        onDispose {
            try {
                exoPlayer.release()
                Timber.d("StoryVideo: Reproductor liberado: storyId=${story.id}")
            } catch (e: Exception) {
                Timber.e(e, "Error al liberar reproductor: storyId=${story.id}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        story.mediaUrl?.let {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isPaused) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = "Play",
                    modifier = Modifier.size(72.dp).align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        } ?: run {
            Timber.w("StoryVideo: No hay URL de video para storyId=${story.id}")
            Text(
                text = "Video no disponible",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}