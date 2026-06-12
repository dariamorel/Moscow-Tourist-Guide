package com.example.touristguide.presentation.ui.my_routes_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.touristguide.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.progressPercent
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteButton
import java.time.format.DateTimeFormatter

@Composable
fun MyRoutesScreen(
    onCreateRouteClick: () -> Unit,
    onRouteClick: (Route) -> Unit,
    onAchievementsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MyRoutesViewModel = viewModel(
        factory = PresentationComponentHolder.component.createMyRoutesViewModel()
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRoutes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val routes by viewModel.routes.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Мои маршруты",
                modifier = Modifier.align(Alignment.CenterStart),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.Black
            )

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onAchievementsClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_medal),
                        contentDescription = "Достижения",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = "Профиль",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        CreateRouteButton(
            onClick = onCreateRouteClick,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(routes.sorted(), key = { it.id }) { route ->
                RouteListItem(
                    route = route,
                    onClick = { onRouteClick(route) }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
            }
        }
    }
}

@Composable
private fun RouteListItem(
    route: Route,
    onClick: () -> Unit
) {
    val isInactive = route.status == Route.Status.PASSED || route.status == Route.Status.DECLINED
    val textColor = if (isInactive) Color(0xFF9E9E9E) else Color.Black
    val progress = route.progressPercent()
    val dateText = route.date?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    text = "Общее время: ${route.totalTime ?: "—"}",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                Text(
                    text = "Пройдено: $progress%",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }

            RouteStatusBadge(status = route.status)
        }

        if (dateText.isNotBlank()) {
            Text(
                text = dateText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun RouteStatusBadge(status: Route.Status) {
    val (backgroundColor, textColor) = when (status) {
        Route.Status.NOT_STARTED -> Color(0xFFE0E0E0) to Color.Black
        Route.Status.IN_PROGRESS -> Color(0xFFFFF59D) to Color(0xFF6D4C00)
        Route.Status.PASSED -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
        Route.Status.DECLINED -> Color(0xFFFFCDD2) to Color(0xFFC62828)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

private fun Route.Status.displayName(): String = when (this) {
    Route.Status.NOT_STARTED -> "Не начат"
    Route.Status.IN_PROGRESS -> "В процессе"
    Route.Status.PASSED -> "Завершен"
    Route.Status.DECLINED -> "Отменен"
}

