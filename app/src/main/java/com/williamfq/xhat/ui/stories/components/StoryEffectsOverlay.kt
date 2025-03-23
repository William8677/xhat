package com.williamfq.xhat.ui.stories.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.williamfq.domain.model.Story
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun StoryEffectsOverlay(story: Story) {
    var effects by remember { mutableStateOf(listOf<Effect>()) }
    val maxEffects = 10
    val effectDuration = 2000L

    LaunchedEffect(Unit) {
        while (true) {
            if (effects.size < maxEffects) {
                val newEffect = Effect(
                    position = Offset(
                        Random.nextFloat() * 1000,
                        Random.nextFloat() * 2000
                    ),
                    type = EffectType.entries.toTypedArray().random()
                )
                effects = effects + newEffect
            }
            delay(200)
            val currentTime = System.currentTimeMillis()
            effects = effects.filter { effect ->
                currentTime - effect.createdAt < effectDuration
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        effects.forEach { effect ->
            val progress = ((System.currentTimeMillis() - effect.createdAt).toFloat() / effectDuration).coerceIn(0f, 1f)
            val alpha = 1f - progress

            when (effect.type) {
                EffectType.SPARKLE -> drawSparkle(effect.position, alpha)
                EffectType.HEART -> drawHeart(effect.position, alpha)
                EffectType.STAR -> drawStar(effect.position, alpha)
            }
        }
    }
}

private fun DrawScope.drawSparkle(position: Offset, alpha: Float) {
    val size = 40f
    val strokeWidth = 2f
    val color = Color.Yellow.copy(alpha = alpha)

    // Dibujar las líneas del destello
    for (i in 0 until 8) {
        val angle = i * Math.PI / 4
        val startX = position.x + size * 0.2f * cos(angle).toFloat()
        val startY = position.y + size * 0.2f * sin(angle).toFloat()
        val endX = position.x + size * 0.5f * cos(angle).toFloat()
        val endY = position.y + size * 0.5f * sin(angle).toFloat()

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }

    // Dibujar el centro del destello
    drawCircle(
        color = color,
        radius = size * 0.15f,
        center = position
    )
}

private fun DrawScope.drawHeart(position: Offset, alpha: Float) {
    val size = 40f
    val color = Color.Red.copy(alpha = alpha)
    val centerX = position.x
    val centerY = position.y

    // Implementación del dibujo del corazón
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(centerX, centerY + size * 0.25f)
        cubicTo(
            centerX - size * 0.5f, centerY - size * 0.4f,
            centerX - size * 0.5f, centerY + size * 0.4f,
            centerX, centerY + size * 0.25f
        )
        cubicTo(
            centerX + size * 0.5f, centerY - size * 0.4f,
            centerX + size * 0.5f, centerY + size * 0.4f,
            centerX, centerY + size * 0.25f
        )
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawStar(position: Offset, alpha: Float) {
    val size = 40f
    val color = Color.Yellow.copy(alpha = alpha)
    val points = 5
    val outerRadius = size / 2
    val innerRadius = outerRadius * 0.4f

    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * Math.PI / points
        val x = position.x + radius * cos(angle).toFloat()
        val y = position.y + radius * sin(angle).toFloat()

        if (i == 0) {
            drawCircle(color, radius = 2f, center = Offset(x, y))
        } else {
            drawLine(
                color = color,
                start = Offset(
                    position.x + (if ((i - 1) % 2 == 0) outerRadius else innerRadius) * cos((i - 1) * Math.PI / points).toFloat(),
                    position.y + (if ((i - 1) % 2 == 0) outerRadius else innerRadius) * sin((i - 1) * Math.PI / points).toFloat()
                ),
                end = Offset(x, y),
                strokeWidth = 2f
            )
        }
    }
}