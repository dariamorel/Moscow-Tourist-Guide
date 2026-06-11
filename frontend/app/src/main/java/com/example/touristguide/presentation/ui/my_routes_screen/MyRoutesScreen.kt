package com.example.touristguide.presentation.ui.my_routes_screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.presentation.di.PresentationComponentHolder

@Composable
fun MyRoutesScreen(
    modifier: Modifier = Modifier,
    onRouteClick: (Route) -> Unit
) {
    val viewModel: MyRoutesViewModel = viewModel(
        factory = PresentationComponentHolder.component.createMyRoutesViewModel()
    )

    Text("My routes", modifier = modifier)
}