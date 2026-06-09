package com.example.touristguide.presentation.ui.create_route_map_screen

import MapContainer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.map_layer.MapMarkers

@Composable
fun CreateRouteMapScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: CreateRouteMapViewModel = viewModel(
        factory = PresentationComponentHolder.component.createCreateRouteMapViewModel()
    )

    val sights by viewModel.sights.collectAsState()
    val mapCenter by viewModel.mapCenter.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        MapContainer(
            mapCenter = mapCenter,
            zoomLevel = zoomLevel,
        ) { mapView ->

            MapMarkers(
                mapView = mapView,
                sights = sights,
                onMapClick = {
                    viewModel.clearSelection()
                },
                onMarkerClick = { sight ->
                    viewModel.selectSight(sight)
                }
            )
        }
    }
}