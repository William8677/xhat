/*
 * Updated: 2025-01-21 20:09:12
 * Author: William8677
 */

package com.williamfq.xhat.ui.filters.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ColorWheel(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }

    LaunchedEffect(color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            color.toArgb(),
            hsv
        )
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rueda de color
        HueWheel(
            hue = hue,
            onHueChange = { newHue ->
                hue = newHue
                val newColor = Color.hsv(hue, saturation, value)
                onColorChange(newColor)
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Control de saturación
        SaturationSlider(
            saturation = saturation,
            hue = hue,
            value = value,
            onSaturationChange = { newSaturation ->
                saturation = newSaturation
                val newColor = Color.hsv(hue, saturation, value)
                onColorChange(newColor)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Control de brillo (value)
        BrightnessSlider(
            value = value,
            hue = hue,
            saturation = saturation,
            onValueChange = { newValue ->
                value = newValue
                val newColor = Color.hsv(hue, saturation, value)
                onColorChange(newColor)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Previsualización del color
        ColorPreview(
            color = Color.hsv(hue, saturation, value),
            onColorChange = onColorChange
        )
    }
}

@Composable
private fun HueWheel(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var center by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val position = change.position
                    val angle = (atan2(
                        position.y - center.y,
                        position.x - center.x
                    ) * 180f / PI).toFloat()
                    val newHue = (angle + 360f) % 360f
                    onHueChange(newHue)
                }
            }
    ) {
        center = Offset(size.width / 2f, size.height / 2f)
        radius = size.width.coerceAtMost(size.height) / 2f - 20.dp.toPx()

        // Dibujar rueda de color
        val stroke = 50.dp.toPx()
        for (angle in 0..360 step 1) {
            val color = Color.hsv(angle.toFloat(), 1f, 1f)
            drawArc(
                color = color,
                startAngle = angle.toFloat(),
                sweepAngle = 1f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke)
            )
        }

        // Dibujar indicador
        val angle = hue * PI.toFloat() / 180f
        val markerRadius = radius
        val markerCenter = Offset(
            center.x + cos(angle) * markerRadius,
            center.y + sin(angle) * markerRadius
        )

        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.hsv(hue, 1f, 1f),
            radius = 10.dp.toPx(),
            center = markerCenter
        )
    }
}

@Composable
private fun SaturationSlider(
    saturation: Float,
    hue: Float,
    value: Float,
    onSaturationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Saturación",
            style = MaterialTheme.typography.bodyMedium
        )

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val x = change.position.x
                        val newSaturation = (x / size.width).coerceIn(0f, 1f)
                        onSaturationChange(newSaturation)
                    }
                }
        ) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.hsv(hue, 0f, value),
                    Color.hsv(hue, 1f, value)
                )
            )

            drawRect(gradientBrush)

            // Indicador
            val x = saturation * size.width
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(x, size.height / 2),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.hsv(hue, saturation, value),
                radius = 6.dp.toPx(),
                center = Offset(x, size.height / 2)
            )
        }
    }
}

@Composable
private fun BrightnessSlider(
    value: Float,
    hue: Float,
    saturation: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = "Brillo",
            style = MaterialTheme.typography.bodyMedium
        )

        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val x = change.position.x
                        val newValue = (x / size.width).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
        ) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Black,
                    Color.hsv(hue, saturation, 1f)
                )
            )

            drawRect(gradientBrush)

            // Indicador
            val x = value * size.width
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(x, size.height / 2),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.hsv(hue, saturation, value),
                radius = 6.dp.toPx(),
                center = Offset(x, size.height / 2)
            )
        }
    }
}

@Composable
private fun ColorPreview(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.medium
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Valores RGB
        Text(
            text = "RGB: (${(color.red * 255).toInt()}, " +
                    "${(color.green * 255).toInt()}, " +
                    "${(color.blue * 255).toInt()})",
            style = MaterialTheme.typography.bodySmall
        )

        // Valor Hex
        Text(
            text = "Hex: #${color.toArgb().toUInt().toString(16).substring(2).uppercase()}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}