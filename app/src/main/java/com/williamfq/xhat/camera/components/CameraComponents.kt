/*
 * Updated: 2025-01-27 23:54:13
 * Author: William8677
 */

package com.williamfq.xhat.ui.camera.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Composable para mostrar la vista de la cámara usando un SurfaceView.
 */
@Composable
fun CameraPreview(
    onSurfaceProvided: (android.view.Surface, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        // No iniciamos la cámara aquí, esperamos a surfaceChanged
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        holder.surface?.let { surface ->
                            onSurfaceProvided(surface, width, height)
                        }
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        // Cleanup si es necesario
                    }
                })
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Botón de captura con animación de "ripple" cuando está capturando.
 */
@Composable
fun CaptureButton(
    isCapturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColorScheme = MaterialTheme.colorScheme

    val scale by animateFloatAsState(
        targetValue = if (isCapturing) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val rippleAlpha by animateFloatAsState(
        targetValue = if (isCapturing) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale)
    ) {
        // Efecto "ripple" exterior
        if (isCapturing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = currentColorScheme.primary.copy(alpha = 1f - rippleAlpha),
                    radius = size.minDimension / 2f
                )
            }
        }

        // Botón principal circular
        CaptureButtonContent(
            isCapturing = isCapturing,
            onClick = onClick,
            currentColorScheme = currentColorScheme
        )
    }
}

@Composable
private fun CaptureButtonContent(
    isCapturing: Boolean,
    onClick: () -> Unit,
    currentColorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .border(
                width = 4.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .background(
                color = currentColorScheme.primary,
                shape = CircleShape
            )
    )
}

/**
 * Círculo de enfoque con fade-out tras un breve delay.
 */
@Composable
fun FocusCircle(
    position: Offset,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        finishedListener = {
            if (!isVisible) onAnimationEnd()
        }
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 1.2f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    LaunchedEffect(position) {
        isVisible = true
        delay(1000)
        isVisible = false
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = position.x.roundToInt(),
                    y = position.y.roundToInt()
                )
            }
            .scale(scale)
    ) {
        Canvas(
            modifier = Modifier.size(64.dp)
        ) {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * Cuadrícula de regla de tercios para ayudar al encuadre.
 */
@Composable
fun CameraGrid(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        val color = Color.White.copy(alpha = 0.3f)

        // Líneas verticales
        drawLine(
            color = color,
            start = Offset(size.width / 3f, 0f),
            end = Offset(size.width / 3f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width * 2f / 3f, 0f),
            end = Offset(size.width * 2f / 3f, size.height),
            strokeWidth = strokeWidth
        )

        // Líneas horizontales
        drawLine(
            color = color,
            start = Offset(0f, size.height / 3f),
            end = Offset(size.width, size.height / 3f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 2f / 3f),
            end = Offset(size.width, size.height * 2f / 3f),
            strokeWidth = strokeWidth
        )
    }
}