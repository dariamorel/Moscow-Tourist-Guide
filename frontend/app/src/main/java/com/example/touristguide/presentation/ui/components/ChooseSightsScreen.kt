package com.example.touristguide.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel
import kotlinx.coroutines.launch

private enum class Tabs(val option: String) {
    MAP("Карта"),
    SEARCH("Поиск")
}

@Composable
fun ChooseSightsScreen(
    mapViewModel: MapViewModel,
    sightsViewModel: SightsViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                modifier = Modifier.statusBarsPadding()
            ) {
                Tabs.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tab.option, color = MaterialTheme.colorScheme.onBackground) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> ChooseSightsMapScreen(mapViewModel, sightsViewModel, Modifier.padding(innerPadding)) { modifier ->
                    var buttonText by remember { mutableStateOf("Добавить") }

                    Button(
                        onClick = {
                            when (buttonText) {
                                "Добавить" -> buttonText = "Удалить"
                                "Удалить" -> buttonText = "Добавить"
                            }
                        },
                        modifier = modifier,
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                1 -> ChooseSightsSearchScreen(sightsViewModel, Modifier.padding(innerPadding)) { modifier ->
                    var buttonText by remember { mutableStateOf("Добавить") }

                    Button(
                        onClick = {
                            when (buttonText) {
                                "Добавить" -> buttonText = "Удалить"
                                "Удалить" -> buttonText = "Добавить"
                            }
                        },
                        modifier = modifier,
                    ) {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}