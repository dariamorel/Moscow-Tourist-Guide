package com.example.touristguide.presentation.ui.optimizing_loading_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.common.ScreenBackButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

private const val MIN_LOADING_TIME_MS = 5000L

@Composable
fun OptimizingLoadingScreen(
    sightIds: List<Int>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onLoadingEnd: (OptimizedSightsOrder) -> Unit
) {
    val viewModel: OptimizingLoadingViewModel = viewModel(
        factory = PresentationComponentHolder.component.createOptimizingLoadingViewModel(sightIds)
    )

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        viewModel.isLoading.filter { !it }.first()

        val result = viewModel.optimizedSights.value ?: return@LaunchedEffect

        val remaining = MIN_LOADING_TIME_MS - (System.currentTimeMillis() - startTime)
        if (remaining > 0) {
            delay(remaining)
        }

        onLoadingEnd(result)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ScreenBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Маршрут оптимизируется по времени обхода достопримечательностей, чтобы ваше путешествие было насыщенным и продуктивным!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            CircularProgressIndicator(
                modifier = Modifier.padding(top = 32.dp),
                color = Color(0xFF757575)
            )
        }
    }
}
