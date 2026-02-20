package com.example.touristguide.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.random.Random
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.presentation.ui.components.routes.RouteSightCard
import com.example.touristguide.presentation.viewModel.SightsViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RouteScreen(
    route: Route,
    sightsViewModel: SightsViewModel,
    modifier: Modifier = Modifier
) {
    val sights = route.order
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (sights.isEmpty()) 1 else sights.size + 1 } // +1 final "completed" page
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Маршрут №${route.id}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "\uD83D\uDD52 ${route.totalTime?.format(DateTimeFormatter.ofPattern("hh ч mm мин"))}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (sights.isEmpty()) {
            Text(
                text = "В этом маршруте пока нет мест.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
            return
        }

        Text(
            text = "${minOf(pagerState.currentPage + 1, sights.size)} / ${sights.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSize = PageSize.Fill,
            pageSpacing = 14.dp
        ) { page ->
            // Last page: celebration screen.
            if (page == sights.size) {
                RouteCompletedPage(modifier = Modifier.fillMaxWidth())
            } else {
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val distance = pageOffset.absoluteValue.coerceIn(0f, 1f)

                // Center card: bigger + more opaque. Side cards: smaller + faded + a bit shifted.
                val scale = 0.92f + (1f - distance) * 0.08f
                val alpha = 0.55f + (1f - distance) * 0.45f

                RouteSightCard(
                    sight = sights[page],
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                            translationX = pageOffset * size.width * 0.08f
                        },
                    onPassedClick = { sightsViewModel.doneSight(page, route) },
                    onDeclinedClick = { sightsViewModel.undoneSight(page, route) }
                )
            }
        }
    }
}

@Composable
private fun RouteCompletedPage(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )
    val popScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "popScale"
    )

    val particles = rememberConfettiParticles()

    Box(
        modifier = modifier
            .padding(30.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            val centerX = size.width / 2f
            val baseY = size.height * 0.82f
            val gravity = size.height * 0.75f

            particles.forEach { p ->
                val local = ((t - p.delay).coerceIn(0f, 1f) / (1f - p.delay).coerceAtLeast(0.001f))
                if (local <= 0f) return@forEach

                val speed = p.speed * size.height
                val dx = kotlin.math.cos(p.angleRad.toDouble()).toFloat()
                val dy = kotlin.math.sin(p.angleRad.toDouble()).toFloat()
                val x = centerX + dx * speed * local * 0.55f
                val y = baseY - dy * speed * local + gravity * local * local

                drawCircle(
                    color = p.color,
                    radius = p.radiusPx * (1f - local * 0.2f),
                    center = Offset(x, y)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.graphicsLayer {
                    scaleX = popScale
                    scaleY = popScale
                }
            )
            Text(
                text = "Маршрут пройден",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private data class ConfettiParticle(
    val angleRad: Float,
    val speed: Float,
    val radiusPx: Float,
    val delay: Float,
    val color: Color,
)

@Composable
private fun rememberConfettiParticles(): List<ConfettiParticle> {
    // Stable per composition (so animation looks consistent).
    return androidx.compose.runtime.remember {
        val rnd = Random(42)
        val palette = listOf(
            Color(0xFF7C4DFF),
            Color(0xFF00C853),
            Color(0xFFFFD600),
            Color(0xFFFF5252),
            Color(0xFF40C4FF),
        )
        List(90) {
            ConfettiParticle(
                angleRad = (Math.toRadians((45 + rnd.nextInt(90)).toDouble())).toFloat(), // 45..134 degrees
                speed = 0.35f + rnd.nextFloat() * 0.55f,
                radiusPx = 3f + rnd.nextFloat() * 3.5f,
                delay = rnd.nextFloat() * 0.35f,
                color = palette[rnd.nextInt(palette.size)]
            )
        }
    }
}