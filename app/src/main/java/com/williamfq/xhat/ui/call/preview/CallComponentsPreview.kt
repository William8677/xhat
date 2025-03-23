package com.williamfq.xhat.ui.call.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.williamfq.domain.model.User
import com.williamfq.xhat.domain.model.FilterType
import com.williamfq.xhat.call.model.CallState
import com.williamfq.xhat.call.model.CallErrorCode
import com.williamfq.xhat.call.model.CallEndReason
import com.williamfq.xhat.call.model.CallQuality
import com.williamfq.xhat.filters.FilterFactory
import com.williamfq.xhat.filters.base.Filter
import com.williamfq.xhat.ui.call.components.*
import com.williamfq.xhat.ui.call.screens.BottomCallControls
import com.williamfq.xhat.ui.theme.XhatTheme

class FilterProvider : PreviewParameterProvider<Filter?> {
    override val values = sequenceOf(
        null,
        FilterFactory.createFilter(FilterType.BLUR),
        FilterFactory.createFilter(FilterType.SEPIA),
        FilterFactory.createFilter(FilterType.GRAYSCALE)
        // Añade más filtros aquí si es necesario
    )
}

class CallStateProvider : PreviewParameterProvider<CallState> {
    override val values = sequenceOf(
        CallState.Idle,
        CallState.Connecting(createPreviewUser(), isVideoCall = true),
        CallState.Connected(createPreviewUser(), isVideoCall = true),
        CallState.Connected(createPreviewUser(), isVideoCall = false),
        CallState.Error(CallErrorCode.CONNECTION_FAILED, "La conexión ha fallado")
    )
}

private fun createPreviewUser() = User(
    id = "user123",
    username = "Usuario Preview",
    avatarUrl = "https://example.com/avatar.jpg"
)

@Preview(
    name = "Call Screen - Light Theme",
    showBackground = true,
    backgroundColor = 0xFF121212,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun PreviewCallScreenLight(
    @PreviewParameter(CallStateProvider::class) callState: CallState
) {
    XhatTheme(darkTheme = false) {
        VideoCallContent(
            callState = callState,
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

@Preview(
    name = "Call Screen - Dark Theme",
    showBackground = true,
    backgroundColor = 0xFF121212,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun PreviewCallScreenDark(
    @PreviewParameter(CallStateProvider::class) callState: CallState
) {
    XhatTheme(darkTheme = true) {
        VideoCallContent(
            callState = callState,
            currentFilter = FilterFactory.createFilter(FilterType.BLUR),
            showGameSelector = true,
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

@Preview(
    name = "Call Controls Preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 100
)
@Composable
fun PreviewCallControls() {
    XhatTheme {
        BottomCallControls(
            isMuted = false,
            isCameraOn = true,
            currentFilter = null,
            onMuteToggle = {},
            onCameraToggle = {},
            onFilterSelect = {},
            onShowGames = {},
            onEndCall = {}
        )
    }
}

@Preview(
    name = "Filter Selector Preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 200
)
@Composable
fun PreviewFilterSelector() {
    XhatTheme {
        FilterSelector(
            currentFilter = FilterFactory.createFilter(FilterType.BLUR),
            onFilterSelected = {}
        )
    }
}

@Preview(
    name = "Game Selector Preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 400
)
@Composable
fun PreviewGameSelector() {
    XhatTheme {
        GameSelector(
            onGameSelected = {},
            onDismiss = {}
        )
    }
}