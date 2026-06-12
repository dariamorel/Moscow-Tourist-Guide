package com.example.touristguide.presentation.ui.route_map_screen

import MapContainer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.map_layer.MapMarkers
import com.example.touristguide.presentation.ui.map_layer.MapRouteLine

@Composable
fun RouteMapScreen(
    route: Route,
    onBackToRoute: () -> Unit,
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

        Surface(
            onClick = onBackToRoute,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(50),
            color = Color.White,
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text(
                text = "Обратно к маршруту",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}
