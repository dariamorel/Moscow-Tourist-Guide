package com.example.touristguide.presentation.ui.route_map_screen

import MapContainer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.common.ScreenBackButton
import com.example.touristguide.presentation.ui.map_layer.MapMarkers
import com.example.touristguide.presentation.ui.map_layer.MapRouteLine

@Composable
fun RouteMapScreen(
    route: Route,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: RouteMapViewModel = viewModel(
        factory = PresentationComponentHolder.component.createRouteMapViewModel(route)
    )

    Box(modifier = modifier.fillMaxSize()) {
        MapContainer(
            mapCenter = viewModel.mapCenter,
            zoomLevel = viewModel.zoomLevel,
            modifier = Modifier.fillMaxSize()
        ) { mapView ->
            MapRouteLine(mapView = mapView, sights = viewModel.sights)
            MapMarkers(
                mapView = mapView,
                sights = viewModel.sights,
                showAllLabels = true
            )
        }

        ScreenBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
