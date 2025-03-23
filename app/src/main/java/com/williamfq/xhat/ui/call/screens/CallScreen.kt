/*
 * Updated: 2025-02-06 00:09:44
 * Author: William8677
 */
package com.williamfq.xhat.ui.call.screens

import android.view.SurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.domain.model.User
import com.williamfq.xhat.R
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.ui.call.viewmodel.CallViewModel
import com.williamfq.xhat.filters.base.Filter

@Composable
fun CallScreen(
    onNavigateBack: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    // Si el estado es Idle, se navega automáticamente hacia atrás
    if (callState is CallState.Idle) {
        LaunchedEffect(Unit) { onNavigateBack() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido principal según el estado de la llamada
        when (val state = callState) {
            is CallState.Connected -> {
                if (state.isVideoCall && state.isVideoEnabled) {
                    ConnectedCallUI(
                        state = state,
                        onEndCall = {
                            viewModel.endCall()
                            onNavigateBack()
                        },
                        onToggleMute = viewModel::toggleMute,
                        onToggleVideo = viewModel::toggleVideo,
                        onSwitchCamera = viewModel::switchCamera,
                        onFilterSelect = viewModel::setFilter,
                        onShowGames = viewModel::showGameSelector
                    )
                } else {
                    AudioCallUI(remoteUser = state.remoteUser)
                }
            }
            is CallState.Connecting -> ConnectingOverlay()
            is CallState.Error -> ErrorCallUI(
                message = state.message,
                onDismiss = onNavigateBack
            )
            is CallState.Ringing -> RingingOverlay(state)
            is CallState.Ended -> EndedOverlay(state)
            else -> { /* No se muestra contenido adicional */ }
        }

        // Controles persistentes (TopBar y BottomControls)
        Column(modifier = Modifier.fillMaxSize()) {
            TopCallBar(
                callState = callState,
                onBackClick = onNavigateBack
            )

            Spacer(modifier = Modifier.weight(1f))

            BottomCallControls(
                isMuted = uiState.isMuted,
                isCameraOn = uiState.isVideoEnabled,
                currentFilter = currentFilter,
                onMuteToggle = viewModel::toggleMute,
                onCameraToggle = viewModel::toggleVideo,
                onFilterSelect = viewModel::setFilter,
                onShowGames = viewModel::showGameSelector,
                onEndCall = {
                    viewModel.endCall()
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
fun TopCallBar(
    callState: CallState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }

            val statusText = when (callState) {
                is CallState.Connected -> stringResource(R.string.connected)
                is CallState.Connecting -> stringResource(R.string.connecting)
                is CallState.Ringing -> stringResource(R.string.ringing)
                is CallState.Error -> callState.message
                is CallState.Ended -> stringResource(R.string.call_ended)
                else -> ""
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Placeholder para centrar
            Box(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun BottomCallControls(
    isMuted: Boolean,
    isCameraOn: Boolean,
    currentFilter: Filter?,
    onMuteToggle: () -> Unit,
    onCameraToggle: () -> Unit,
    onFilterSelect: (FilterType) -> Unit,
    onShowGames: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Los botones ofrecen feedback visual (cambia el tint si están activos)
            CallControlButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Toggle Mute",
                onClick = onMuteToggle,
                isActive = isMuted
            )
            CallControlButton(
                icon = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = "Toggle Camera",
                onClick = onCameraToggle,
                isActive = !isCameraOn
            )
            CallControlButton(
                icon = Icons.Default.FilterAlt,
                contentDescription = "Filters",
                onClick = { onFilterSelect(FilterType.BLUR) }, // Ejemplo: se pasa BLUR
                isActive = currentFilter != null
            )
            CallControlButton(
                icon = Icons.Default.SportsEsports,
                contentDescription = "Games",
                onClick = onShowGames
            )
            CallControlButton(
                icon = Icons.Default.CallEnd,
                contentDescription = "End Call",
                onClick = onEndCall,
                backgroundColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = backgroundColor,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// UIs para cada estado de llamada

@Composable
fun ConnectedCallUI(
    state: CallState.Connected,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onFilterSelect: (FilterType) -> Unit,
    onShowGames: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isVideoCall && state.isVideoEnabled) {
            // Vista remota con filtros aplicados (si se desea, se puede integrar la lógica de filtros)
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        // Configuración de la vista remota
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            // Vista local (PiP)
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        // Configuración de la vista local
                    }
                },
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        } else {
            AudioCallUI(remoteUser = state.remoteUser)
        }
    }
}

@Composable
fun AudioCallUI(remoteUser: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = remoteUser.username,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Llamada en curso",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ConnectingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun RingingOverlay(state: CallState.Ringing) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Llamada entrante de ${state.remoteUser.username}",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun EndedOverlay(state: CallState.Ended) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Llamada terminada. Duración: ${state.duration} ms",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun ErrorCallUI(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar")
        }
    }
}
