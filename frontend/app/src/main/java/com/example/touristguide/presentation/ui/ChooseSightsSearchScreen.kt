package com.example.touristguide.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.touristguide.Screens
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.ui.components.map.ShowSightDetail
import com.example.touristguide.presentation.viewModel.SightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseSightsSearchScreen(
    navController: NavController,
    viewModel: SightsViewModel,
    modifier: Modifier = Modifier,
    cardButton: @Composable (Modifier, Sight) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val querySights by viewModel.querySights.collectAsState()
    val categoriesList by viewModel.categoriesList.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        bottomBar = {
            Button(
                onClick = {
                    viewModel.createRoute()
                    navController.navigate(Screens.MY_ROUTES.title)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Создать маршрут",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Создание маршрута",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchSights(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                singleLine = true,
                placeholder = { Text("Поиск достопримечательностей") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(
                            onClick = {
                                query = ""
                                viewModel.searchSights("")
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (query.isBlank()) {
                    items(categoriesList.size) { index ->
                        val category = categoriesList[index]
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "Показать места этой категории",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = category
                                    viewModel.searchSightByCategory(category)
                                }
                        )
                    }
                } else {
                    if (querySights.isEmpty()) {
                        item {
                            Text(
                                text = "Ничего не найдено",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(querySights.size) { index ->
                            val sight = querySights[index]
                            ShowSightDetail(
                                sight = sight,
                                modifier = Modifier.fillMaxWidth(),
                                button = cardButton
                            )
                        }
                    }
                }
            }
        }
    }
}