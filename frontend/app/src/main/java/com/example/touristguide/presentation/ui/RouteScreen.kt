package com.example.touristguide.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
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
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { sights.size.coerceAtLeast(1) })
    val allRoutes by sightsViewModel.allRoutes.collectAsState()

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
            text = "${pagerState.currentPage + 1} / ${sights.size}",
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
            val pageOffset =
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val distance = pageOffset.absoluteValue.coerceIn(0f, 1f)

            val scale = 0.96f + (1f - distance) * 0.12f
            val alpha = 0.7f + (1f - distance) * 0.3f

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