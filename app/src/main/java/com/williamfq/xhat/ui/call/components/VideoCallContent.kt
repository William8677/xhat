package com.williamfq.xhat.ui.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.ui.common.AutoHideSnackbar
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.ui.call.screens.BottomCallControls
import com.williamfq.xhat.ui.call.screens.ConnectingOverlay
import com.williamfq.xhat.ui.call.screens.TopCallBar

@Composable
fun VideoCallContent(
    callState: CallState,
    currentFilter: Filter?,
    showGameSelector: Boolean,
    isMuted: Boolean,
    isCameraOn: Boolean,
    localVideoTrack: VideoTrack?,
    remoteVideoTrack: VideoTrack?,
    onNavigateToGame: (String) -> Unit,
    onEndCall: () -> Unit,
    onMuteToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onFilterSelect: (FilterType) -> Unit,
    onShowGames: () -> Unit,
    onHideGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Observar errores y mostrar Snackbar
    LaunchedEffect(callState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (callState is CallState.Error) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = callState.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Renderer
        VideoRenderer(
            localVideoTrack = localVideoTrack,
            remoteVideoTrack = remoteVideoTrack,
            filter = currentFilter,
            modifier = Modifier.fillMaxSize()
        )

        // Controles superpuestos
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopCallBar(
                callState = callState,
                onBackClick = onEndCall
            )

            // Área central - puede mostrar el selector de juegos
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showGameSelector) {
                    GameSelector(
                        onGameSelected = onNavigateToGame,
                        onDismiss = onHideGames,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                // Indicador de estado de conexión
                if (callState is CallState.Connecting) {
                    ConnectingOverlay()
                }
            }

            // Controles inferiores
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de filtros (si la cámara está activada)
                if (isCameraOn) {
                    FilterSelector(
                        currentFilter = currentFilter,
                        onFilterSelected = { filterType ->
                            val filter = FilterFactory.createFilter(filterType)
                            onFilterSelect(filterType)
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Controles principales
                BottomCallControls(
                    isMuted = isMuted,
                    isCameraOn = isCameraOn,
                    currentFilter = currentFilter,
                    onMuteToggle = onMuteToggle,
                    onCameraToggle = onCameraToggle,
                    onFilterSelect = onFilterSelect,
                    onShowGames = onShowGames,
                    onEndCall = onEndCall
                )
            }
        }

        // Snackbar para errores
        AutoHideSnackbar(
            snackbarHostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 88.dp) // Por encima de los controles
        )
    }
}

@Composable
private fun CallDurationTimer(
    startTime: Long,
    modifier: Modifier = Modifier
) {
    var duration by remember { mutableStateOf(0L) }

    LaunchedEffect(startTime) {
        while (true) {
            duration = System.currentTimeMillis() - startTime
            kotlinx.coroutines.delay(1000)
        }
    }

    Text(
        text = formatDuration(duration),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

private fun formatDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60)) % 60
    val hours = duration / (1000 * 60 * 60)

    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}