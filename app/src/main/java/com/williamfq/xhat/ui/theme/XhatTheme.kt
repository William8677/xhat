// XhatTheme.kt
package com.williamfq.xhat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun XhatTheme(content: @Composable () -> Unit) {
    val LightColors = lightColorScheme(
        primary = Color(0xFF6200EE),
        onPrimary = Color.White,
        secondary = Color(0xFF03DAC6),
        onSecondary = Color.Black,
        background = Color(0xFFFFFFFF),
        onBackground = Color.Black,
        surface = Color(0xFFFFFFFF),
        onSurface = Color.Black
        // Puedes definir otros colores según tus necesidades
    )

    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography, // Referencia a Typography.kt
        shapes = Shapes,         // Referencia a Shapes.kt
        content = content
    )
}
