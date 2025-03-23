package com.williamfq.xhat.ui.stories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.williamfq.domain.model.Story

@Composable
fun StoryAudio(
    story: Story,
    onProgressUpdate: (Float) -> Unit,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var duration by remember { mutableLongStateOf(0L) }
    var currentPosition by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(story.mediaUrl ?: return@apply))
            prepare()
            playWhenReady = !isPaused
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    if (state == Player.STATE_READY) {
                        duration = this@apply.duration
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    currentPosition = this@apply.currentPosition
                    if (duration > 0) {
                        onProgressUpdate(currentPosition.toFloat() / duration.toFloat())
                    }
                }
            })
        }
    }

    DisposableEffect(isPaused) {
        if (isPaused) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = story.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = { if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
            ) {
                Icon(
                    imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (exoPlayer.isPlaying) "Pausar" else "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}