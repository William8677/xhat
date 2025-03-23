package com.williamfq.xhat.ui.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.williamfq.xhat.domain.model.CallRecord
import com.williamfq.xhat.domain.model.CallType
import com.williamfq.xhat.domain.model.CallStatus  // Asegúrate de que este enum exista en el paquete indicado
import com.williamfq.xhat.ui.calls.viewmodel.CallHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Llamadas") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn {
                    items(
                        items = uiState.calls,
                        key = { it.id }
                    ) { call ->
                        CallHistoryItem(
                            call = call,
                            onItemClick = { onNavigateToChat(call.callerUserId) },
                            onVoiceCallClick = {
                                viewModel.makeCall(call.callerUserId, CallType.VOICE)
                            },
                            onVideoCallClick = {
                                viewModel.makeCall(call.callerUserId, CallType.VIDEO)
                            }
                        )
                    }
                }
            }

            // Error Snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun CallHistoryItem(
    call: CallRecord,
    onItemClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatterWithDay = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val isToday = remember(call.startTime) {
        Calendar.getInstance().apply {
            timeInMillis = call.startTime
        }.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    }

    ListItem(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        leadingContent = {
            AsyncImage(
                model = call.callerProfileImage,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        },
        headlineContent = {
            Text(
                text = call.callerUsername.takeIf { it.isNotEmpty() }
                    ?: call.callerPhoneNumber
                    ?: "Usuario desconocido",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (call.status) {
                        CallStatus.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                        CallStatus.REJECTED -> Icons.Default.CallEnd
                        CallStatus.ANSWERED -> Icons.Default.Call
                        CallStatus.BUSY -> Icons.Default.PhoneLocked
                        else -> Icons.Default.Call
                    },
                    contentDescription = null,
                    tint = when (call.status) {
                        CallStatus.MISSED, CallStatus.REJECTED -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isToday) {
                        dateFormatter.format(Date(call.startTime))
                    } else {
                        dateFormatterWithDay.format(Date(call.startTime))
                    }
                )
                if (call.duration > 0) {
                    Text(" • ${formatDuration(call.duration)}")
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onVoiceCallClick) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Llamar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onVideoCallClick) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Video llamada",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}
