package com.williamfq.xhat.ui.stories.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Close
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
fun StoryQuiz(
    story: Story,
    viewModel: StoriesViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val quizOptions = story.quizOptions ?: story.options ?: emptyList()
    val correctAnswerIndex = story.correctAnswer ?: -1
    val scope = rememberCoroutineScope()

    if (quizOptions.isEmpty()) {
        Timber.w("StoryQuiz: No hay opciones de quiz para storyId=${story.id}")
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(story.backgroundColor ?: Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error: No hay opciones de quiz",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    if (correctAnswerIndex !in quizOptions.indices) {
        Timber.w("StoryQuiz: Índice de respuesta correcta inválido para storyId=${story.id}")
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
            text = story.quizQuestion ?: story.question ?: "Quiz sin título",
            style = MaterialTheme.typography.headlineSmall,
            color = story.textColor ?: Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        quizOptions.forEachIndexed { index, option ->
            val isSelected = selectedOption == option
            val isCorrect = index == correctAnswerIndex
            val backgroundColor = when {
                selectedOption != null && isSelected && isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                selectedOption != null && isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            }

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
                                        type = InteractionType.QUIZ_ANSWER,
                                        content = "Respondió $option",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                viewModel.handleReaction(
                                    storyId = story.id,
                                    reaction = Reaction(
                                        id = UUID.randomUUID().toString(),
                                        userId = currentUserId!!,
                                        type = if (isCorrect) ReactionType.HUNDRED else ReactionType.SAD,
                                        content = if (isCorrect) "Respuesta correcta" else "Respuesta incorrecta",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                Timber.d("Respuesta en quiz: storyId=${story.id}, opción=$option, correcta=$isCorrect")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al procesar respuesta en quiz: storyId=${story.id}")
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
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
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = if (isCorrect) "Correcto" else "Incorrecto",
                            tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp).padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        story.explanation?.let { explanation ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Explicación: $explanation",
                style = MaterialTheme.typography.bodyMedium,
                color = story.textColor ?: Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .animateContentSize()
            )
        }
    }
}
