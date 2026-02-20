package com.example.touristguide.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var categoryQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val querySights by viewModel.querySights.collectAsState()
    val categoriesList by viewModel.categoriesList.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val onActiveChange = { expanded: Boolean -> active = expanded }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Создание маршрута",
            fontSize = 20.sp,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { 
                        query = it
                        viewModel.searchSights(it)
                    },
                    onSearch = { active = false },
                    expanded = active,
                    onExpandedChange = onActiveChange,
                    placeholder = { Text("Поиск достопримечательностей") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                )
            },
            expanded = active,
            onExpandedChange = onActiveChange,
            modifier = Modifier
                .fillMaxWidth(),
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (query.isBlank()) {
                        items(categoriesList.size) { index ->
                            val category = categoriesList[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        categoryQuery = category
                                        query = category
                                        viewModel.searchSightByCategory(category)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
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
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.createRoute()
                navController.navigate(Screens.MY_ROUTES.title) },
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Создать маршрут",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}