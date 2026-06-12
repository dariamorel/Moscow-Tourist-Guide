package com.example.touristguide.presentation.ui.go_route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.di.PresentationComponentHolder

@Composable
fun GoRouteScreen(
    route: Route,
    onClose: () -> Unit,
    onOpenSight: (Sight) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GoRouteViewModel = viewModel(
        factory = PresentationComponentHolder.component.createGoRouteViewModel(route)
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshIsPassed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isStarted by viewModel.isStarted.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isPassedMap by viewModel.isPassed.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        GoRouteHeader(
            title = viewModel.routeName,
            onClose = onClose
        )

        if (!isStarted) {
            StartRouteContent(onStart = viewModel::startRoute)
        } else {
            ActiveRouteContent(
                sights = viewModel.sights,
                currentIndex = currentIndex,
                isPassedMap = isPassedMap,
                canGoPrevious = viewModel.canGoPrevious(currentIndex),
                canGoNext = viewModel.canGoNext(currentIndex),
                onPrevious = viewModel::goToPrevious,
                onNext = viewModel::goToNext,
                onOpenSight = onOpenSight,
                onDeclineRoute = { viewModel.declineRoute(onClose) }
            )
        }
    }
}

@Composable
private fun GoRouteHeader(
    title: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }

    HorizontalDivider(color = Color(0xFFE0E0E0))
}

@Composable
private fun StartRouteContent(onStart: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onStart,
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = Color(0xFFC8E6C9)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "СТАРТ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun ActiveRouteContent(
    sights: List<Sight>,
    currentIndex: Int,
    isPassedMap: Map<Int, Boolean>,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenSight: (Sight) -> Unit,
    onDeclineRoute: () -> Unit
) {
    val currentSight = sights.getOrNull(currentIndex) ?: return
    val isPassed = isPassedMap[currentSight.id] == true

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RouteArrowButton(
                enabled = canGoPrevious,
                onClick = onPrevious,
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
            )

            SightRouteCard(
                sight = currentSight,
                isPassed = isPassed,
                onOpenClick = { onOpenSight(currentSight) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            RouteArrowButton(
                enabled = canGoNext,
                onClick = onNext,
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            )
        }

        Surface(
            onClick = onDeclineRoute,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(50),
            color = Color.White,
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(
                text = "Отменить маршрут",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun RouteArrowButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    val tint = if (enabled) Color.Black else Color(0xFFBDBDBD)

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
        tint = tint
    )
}

@Composable
private fun SightRouteCard(
    sight: Sight,
    isPassed: Boolean,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isPassed) Color(0xFF757575) else Color.Black

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AsyncImage(
                    model = sight.image,
                    contentDescription = sight.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = sight.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )

                sight.address?.takeIf { it.isNotBlank() }?.let { address ->
                    Text(
                        text = address,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPassed) Color(0xFFBDBDBD) else Color(0xFF2B2B2B),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isPassed) "Посмотреть" else "Открыть точку",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        if (isPassed) {
            PassedSightBadge(
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun PassedSightBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(top = 8.dp, end = 8.dp),
        shape = CircleShape,
        color = Color(0xFFC8E6C9)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .padding(6.dp)
                .size(18.dp),
            tint = Color.Black
        )
    }
}
