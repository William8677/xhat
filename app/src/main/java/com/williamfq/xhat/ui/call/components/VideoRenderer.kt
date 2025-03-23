package com.williamfq.xhat.ui.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.VideoTrack
import org.webrtc.SurfaceViewRenderer
import org.webrtc.EglBase
import com.williamfq.xhat.filters.base.Filter

@Composable
fun VideoRenderer(
    localVideoTrack: VideoTrack?,
    remoteVideoTrack: VideoTrack?,
    filter: Filter?,
    modifier: Modifier = Modifier
) {
    val eglBaseContext = EglBase.create().eglBaseContext

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        remoteVideoTrack?.let { track ->
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(eglBaseContext, null)
                        track.addSink(this)
                        filter?.applyToRenderer(this) // Aplicar filtro si está seleccionado
                    }
                },
                modifier = Modifier.matchParentSize()
            )
        }

        localVideoTrack?.let { track ->
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(eglBaseContext, null)
                        track.addSink(this)
                        filter?.applyToRenderer(this) // Aplicar filtro si está seleccionado
                    }
                },
                modifier = Modifier.matchParentSize()
            )
        }
    }
}