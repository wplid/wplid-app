package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class Particle(
    val x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    triggerTime: Long,
    modifier: Modifier = Modifier
) {
    if (triggerTime == 0L) return

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    val progress = remember(triggerTime) { Animatable(0f) }

    val particles = remember(triggerTime) {
        val colors = listOf(
            Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B),
            Color(0xFFEF4444), Color(0xFFA855F7), Color(0xFF3B82F6)
        )
        List(60) {
            Particle(
                x = screenWidthPx / 2f + Random.nextInt(-100, 100),
                y = screenHeightPx / 3f,
                velocityX = Random.nextFloat() * 1200f - 600f,
                velocityY = Random.nextFloat() * -1200f - 300f,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 18f + 8f,
                rotationSpeed = Random.nextFloat() * 720f - 360f
            )
        }
    }

    LaunchedEffect(triggerTime) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val gravity = 2200f

            particles.forEach { p ->
                val currentX = p.x + p.velocityX * t
                val currentY = p.y + p.velocityY * t + 0.5f * gravity * t * t
                val alpha = (1f - t).coerceIn(0f, 1f)

                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(currentX, currentY),
                    size = Size(p.size, p.size * 0.7f)
                )
            }
        }
    }
}
