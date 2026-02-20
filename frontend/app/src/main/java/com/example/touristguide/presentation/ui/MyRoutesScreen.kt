package com.example.touristguide.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.touristguide.Screens
import com.example.touristguide.presentation.ui.components.routes.RouteItem
import com.example.touristguide.presentation.viewModel.SightsViewModel

@Composable
fun MyRoutesScreen(
    navController: NavController,
    viewModel: SightsViewModel,
    modifier: Modifier = Modifier
) {
    val allRoutes by viewModel.allRoutes.collectAsState()

    Column(
        modifier.fillMaxWidth().padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Мои маршруты",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(
            onClick = { navController.navigate(Screens.CREATE_ROUTE.title) },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Добавить маршрут",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            allRoutes.forEachIndexed { i,  route ->
                item {
                    RouteItem(route, i + 1, Modifier.clickable {
                        navController.navigate("${Screens.ACTIVE_ROUTE.title}/${route.id}")
                    })
                }
            }
        }
    }
}