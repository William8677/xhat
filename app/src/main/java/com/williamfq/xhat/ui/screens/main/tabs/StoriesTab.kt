
package com.williamfq.xhat.ui.screens.main.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.runtime.Composable
import com.williamfq.xhat.ui.screens.main.components.EmptyScreenPlaceholder

@Composable
fun StoriesTab() {
    EmptyScreenPlaceholder(
        icon = Icons.Default.PlayCircle,
        text = "Historias"
    )
}