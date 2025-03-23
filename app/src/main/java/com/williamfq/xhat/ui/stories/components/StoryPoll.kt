package com.williamfq.xhat.ui.stories.components

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.nativead.NativeAd
import com.williamfq.domain.model.*
import com.williamfq.domain.repository.StoryRepository
import com.williamfq.domain.repository.UserRepository
import com.williamfq.xhat.ui.stories.AnalyticsManagerInterface
import com.williamfq.xhat.ui.stories.E2EEncryptionInterface
import com.williamfq.xhat.ui.stories.MediaCompressorInterface
import com.williamfq.xhat.ui.stories.MediaDownloadManagerInterface
import com.williamfq.xhat.ui.stories.MessageRepositoryInterface
import com.williamfq.xhat.ui.stories.NativeStoryAdManagerInterface
import com.williamfq.xhat.ui.stories.ShareManagerInterface
import com.williamfq.xhat.ui.stories.viewmodel.StoriesViewModel
import com.williamfq.xhat.utils.logging.LogLevel
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.metrics.EngagementType
import com.williamfq.xhat.utils.metrics.StoryMetricsReport
import com.williamfq.xhat.utils.metrics.StoryMetricsTracker
import com.williamfq.xhat.utils.share.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID
import androidx.core.net.toUri

@Composable
fun StoryPoll(
    story: Story,
    viewModel: StoriesViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val pollOptions = story.pollOptions ?: story.options ?: emptyList()
    val votes = story.votes ?: List(pollOptions.size) { 0 }
    val totalVotes = votes.sum()
    val scope = rememberCoroutineScope()

    if (pollOptions.isEmpty()) {
        Timber.w("StoryPoll: No hay opciones de encuesta para storyId=${story.id}")
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(story.backgroundColor ?: Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: No hay opciones de encuesta",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(story.backgroundColor ?: Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = story.pollQuestion ?: story.question ?: "Encuesta sin título",
            style = MaterialTheme.typography.headlineSmall,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        pollOptions.forEachIndexed { index, option ->
            val voteCount = votes.getOrElse(index) { 0 }
            val percentage = if (totalVotes > 0) (voteCount.toFloat() / totalVotes * 100).toInt() else 0
            val isSelected = selectedOption == option

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .animateContentSize(animationSpec = tween(300))
                    .clickable(enabled = selectedOption == null && currentUserId != null) {
                        selectedOption = option
                        scope.launch {
                            try {
                                viewModel.addInteraction(
                                    story.id,
                                    StoryInteraction(
                                        userId = currentUserId!!,
                                        type = InteractionType.POLL_VOTE,
                                        content = "Votó por $option",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                Timber.d("Voto registrado en encuesta: storyId=${story.id}, opción=$option")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al registrar voto en encuesta: storyId=${story.id}")
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = story.textColor ?: Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedOption != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$percentage% ($voteCount)",
                                style = MaterialTheme.typography.bodySmall,
                                color = story.textColor ?: Color.White
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                if (selectedOption != null) {
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Total de votos: $totalVotes",
            style = MaterialTheme.typography.bodySmall,
            color = story.textColor ?: Color.White.copy(alpha = 0.7f)
        )
    }
}

