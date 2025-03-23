package com.williamfq.xhat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF86320),       // Naranja
    secondary = Color(0xFF121212),     // Negro más profundo para contraste en Dark Theme
    tertiary = Color(0xFFFFB703),      // Naranja más claro
    background = Color(0xFF121212),    // Negro
    surface = Color(0xFF121212),       // Negro
    onPrimary = Color.White,           // Texto sobre Naranja
    onSecondary = Color.White,         // Texto sobre Negro
    onTertiary = Color.Black,          // Texto sobre Naranja más claro
    onBackground = Color.White,        // Texto sobre Negro
    onSurface = Color.White            // Texto sobre Negro
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF86320),       // Naranja
    secondary = Color(0xFFFFFFFF),     // Blanco
    tertiary = Color(0xFFFFB703),      // Naranja más claro
    background = Color(0xFFFFFFFF),    // Blanco
    surface = Color(0xFFFFFFFF),       // Blanco
    onPrimary = Color.White,           // Texto sobre Naranja
    onSecondary = Color.Black,         // Texto sobre Blanco
    onTertiary = Color.Black,          // Texto sobre Naranja más claro
    onBackground = Color.Black,        // Texto sobre Blanco
    onSurface = Color.Black            // Texto sobre Blanco
)

@Composable
fun XhatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
