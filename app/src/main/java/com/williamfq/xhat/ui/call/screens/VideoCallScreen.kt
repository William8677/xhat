package com.williamfq.xhat.ui.call.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.domain.model.User
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.ui.call.components.*
import com.williamfq.xhat.ui.call.viewmodel.CallViewModel
import com.williamfq.xhat.ui.theme.XhatTheme
import org.webrtc.VideoTrack
import kotlin.reflect.KFunction1

@Composable
fun VideoCallScreen(
    viewModel: CallViewModel = hiltViewModel(),
    onNavigateToGame: (String) -> Unit
) {
    val callState by viewModel.callState.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val showGameSelector by viewModel.showGameSelector.collectAsState()

    VideoCallContent(
        callState = callState,
        currentFilter = currentFilter,
        showGameSelector = showGameSelector,
        isMuted = viewModel.isMuted.collectAsState().value,
        isCameraOn = viewModel.isCameraOn.collectAsState().value,
        localVideoTrack = viewModel.localVideoTrack.collectAsState().value,
        remoteVideoTrack = viewModel.remoteVideoTrack.collectAsState().value,
        onNavigateToGame = onNavigateToGame,
        onEndCall = viewModel::endCall,
        onMuteToggle = viewModel::toggleMute,
        onCameraToggle = viewModel::toggleCamera,
        onFilterSelect = viewModel::setFilter,
        onShowGames = viewModel::showGameSelector,
        onHideGames = viewModel::hideGameSelector
    )
}

@Composable
private fun VideoCallContent(
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
    onFilterSelect: KFunction1<Filter, Unit>,
    onShowGames: () -> Unit,
    onHideGames: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Video principal
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

            if (showGameSelector) {
                GameSelector(
                    onGameSelected = onNavigateToGame,
                    onDismiss = onHideGames
                )
            }

            BottomCallControls(
                isMuted = isMuted,
                isCameraOn = isCameraOn,
                currentFilter = currentFilter,
                onMuteToggle = onMuteToggle,
                onCameraToggle = onCameraToggle,
                onFilterSelect = { filterType ->
                    val filter = FilterFactory.createFilter(filterType)
                    onFilterSelect(filterType)
                },
                onShowGames = onShowGames,
                onEndCall = onEndCall
            )
        }

        if (callState is CallState.Connecting) {
            ConnectingOverlay()
        }

        FilterSelector(
            currentFilter = currentFilter,
            onFilterSelected = { filterType ->
                val filter = FilterFactory.createFilter(filterType)
                onFilterSelect(filterType)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

fun onFilterSelect(filterType: FilterType) {

}

// Previews
@Preview(showBackground = true)
@Composable
fun PreviewVideoCallConnected() {
    XhatTheme {
        VideoCallContent(
            callState = CallState.Connected(
                remoteUser = User(
                    id = "user123",
                    username = "John Doe",
                    avatarUrl = "https://example.com/avatar.jpg"
                ),
                isVideoCall = true
            ),
            currentFilter = null,
            showGameSelector = false,
            isMuted = false,
            isCameraOn = true,
            localVideoTrack = null,
            remoteVideoTrack = null,
            onNavigateToGame = {},
            onEndCall = {},
            onMuteToggle = {},
            onCameraToggle = {},
            onFilterSelect = {},
            onShowGames = {},
            onHideGames = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVideoCallConnecting() {
    XhatTheme {
        VideoCallContent(
            callState = CallState.Connecting(
                remoteUser = User(
                    id = "user123",
                    username = "John Doe",
                    avatarUrl = "https://example.com/avatar.jpg"
                ),
                isVideoCall = true
            ),
            currentFilter = null,
            showGameSelector = false,
            isMuted = false,
            isCameraOn = true,
            localVideoTrack = null,
            remoteVideoTrack = null,
            onNavigateToGame = {},
            onEndCall = {},
            onMuteToggle = {},
            onCameraToggle = {},
            onFilterSelect = {},
            onShowGames = {},
            onHideGames = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVideoCallWithGameSelector() {
    XhatTheme {
        VideoCallContent(
            callState = CallState.Connected(
                remoteUser = User(
                    id = "user123",
                    username = "John Doe",
                    avatarUrl = "https://example.com/avatar.jpg"
                ),
                isVideoCall = true
            ),
            currentFilter = null,
            showGameSelector = true,
            isMuted = false,
            isCameraOn = true,
            localVideoTrack = null,
            remoteVideoTrack = null,
            onNavigateToGame = {},
            onEndCall = {},
            onMuteToggle = {},
            onCameraToggle = {},
            onFilterSelect = {},
            onShowGames = {},
            onHideGames = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVideoCallWithFilter() {
    XhatTheme {
        VideoCallContent(
            callState = CallState.Connected(
                remoteUser = User(
                    id = "user123",
                    username = "John Doe",
                    avatarUrl = "https://example.com/avatar.jpg"
                ),
                isVideoCall = true
            ),
            currentFilter = FilterFactory.createFilter(FilterType.BLUR),
            showGameSelector = false,
            isMuted = true,
            isCameraOn = false,
            localVideoTrack = null,
            remoteVideoTrack = null,
            onNavigateToGame = {},
            onEndCall = {},
            onMuteToggle = {},
            onCameraToggle = {},
            onFilterSelect = {},
            onShowGames = {},
            onHideGames = {}
        )
    }
}