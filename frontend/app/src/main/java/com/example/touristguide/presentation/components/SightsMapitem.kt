package com.example.touristguide.presentation.components

import MapContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel

@Composable
fun SightsMapItem(
    mapViewModel: MapViewModel,
    sightsViewModel: SightsViewModel,
    modifier: Modifier = Modifier
) {
    val sights by sightsViewModel.allSights.collectAsState()

    MapContainer(mapViewModel) { mapView ->
        MapMarkers(
            mapView = mapView,
            sights = sights,
            onMarkerClick = { sightsViewModel.onSightSelected(it) }
        )
    }
}