package com.example.touristguide.presentation.ui.go_route

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FireworkParticle(
    val originX: Float,
    val originY: Float,
    val angle: Float,
    val speed: Float,
    val color: Color,
    val radius: Float,
    val startTime: Long,
    val lifeMs: Long
)

private val fireworkColors = listOf(
    Color(0xFFFFEB3B),
    Color(0xFFFF9800),
    Color(0xFFFF5722),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF03A9F4),
    Color(0xFF4CAF50)
)

@Composable
fun FireworksEffect(modifier: Modifier = Modifier) {
    val particles = rememberFireworkParticles()
    var animationTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastBurstTime = 0L
        while (true) {
            withFrameMillis { time ->
                animationTime = time
                if (time - lastBurstTime >= 650L) {
                    lastBurstTime = time
                    spawnBurst(particles, time)
                }
                particles.removeAll { time - it.startTime > it.lifeMs }
            }
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val elapsed = animationTime - particle.startTime
            if (elapsed !in 0..particle.lifeMs) return@forEach

            val progress = elapsed.toFloat() / particle.lifeMs
            val distance = particle.speed * elapsed / 1000f
            val centerX = particle.originX * size.width
            val centerY = particle.originY * size.height
            val x = centerX + cos(particle.angle) * distance
            val y = centerY + sin(particle.angle) * distance + progress * progress * 120f
            val alpha = (1f - progress).coerceIn(0f, 1f)

            drawLine(
                color = particle.color.copy(alpha = alpha * 0.5f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = particle.radius
            )
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.radius * (1f - progress * 0.3f),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun rememberFireworkParticles() = remember {
    mutableStateListOf<FireworkParticle>()
}

private fun spawnBurst(
    particles: MutableList<FireworkParticle>,
    time: Long
) {
    val originX = Random.nextFloat() * 0.7f + 0.15f
    val originY = Random.nextFloat() * 0.45f + 0.08f
    val color = fireworkColors.random()
    val particleCount = Random.nextInt(18, 28)

    repeat(particleCount) {
        particles.add(
            FireworkParticle(
                originX = originX,
                originY = originY,
                angle = Random.nextFloat() * 6.2831855f,
                speed = Random.nextFloat() * 180f + 120f,
                color = color,
                radius = Random.nextFloat() * 2.5f + 2f,
                startTime = time,
                lifeMs = Random.nextLong(1200L, 1800L)
            )
        )
    }
}
