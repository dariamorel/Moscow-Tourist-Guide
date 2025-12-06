package com.example.touristguide.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.touristguide.domain.entities.Sight
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapMarkers(
    mapView: MapView,
    sights: List<Sight>,
    onMarkerClick: (Sight) -> Unit
) {
    val markersOverlay = FolderOverlay()

    LaunchedEffect(sights) {
        sights.forEach { sight ->
            val marker = Marker(mapView).apply {
                position = sight.coords
                title = sight.title
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(sight)
                    true
                }
            }
            markersOverlay.add(marker)
        }

        mapView.overlays.add(markersOverlay)
    }
}