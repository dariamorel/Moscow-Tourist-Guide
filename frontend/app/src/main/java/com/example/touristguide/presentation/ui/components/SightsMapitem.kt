package com.example.touristguide.presentation.ui.components

import MapContainer
import android.graphics.Point
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun SightsMapItem(
    mapViewModel: MapViewModel,
    sightsViewModel: SightsViewModel,
    modifier: Modifier = Modifier,
    cardButton: @Composable (Modifier) -> Unit = {}
) {
    val sights by sightsViewModel.allSights.collectAsState()
    val selectedSight by sightsViewModel.selectedSight.collectAsState()
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxSize()) {
        MapContainer(mapViewModel) { mapView ->
            MapMarkers(
                mapView = mapView,
                sights = sights,
                onMapClick = { sightsViewModel.onDismissSightDetail() }
            ) { sightsViewModel.onSightSelected(it) }

            selectedSight?.let { sight ->
                var popupPosition by remember { mutableStateOf(Point(0, 0)) }
                var cardHeight by remember { mutableStateOf(0) }

                DisposableEffect(mapView, sight) {
                    val listener = object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val p = Point()
                            mapView.projection.toPixels(sight.coords, p)
                            popupPosition = p
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val p = Point()
                            mapView.projection.toPixels(sight.coords, p)
                            popupPosition = p
                            return true
                        }
                    }
                    mapView.addMapListener(listener)

                    val p = Point()
                    mapView.projection.toPixels(sight.coords, p)
                    popupPosition = p

                    onDispose {
                        mapView.removeMapListener(listener)
                    }
                }

                val offsetX = with(density) { -150.dp.toPx() }.toInt()
                val offsetY = with(density) { -cardHeight - 60.dp.toPx() }.toInt()

                ShowSightDetail(
                    sight,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            cardHeight = coordinates.size.height
                        }
                        .absoluteOffset {
                            IntOffset(
                                x = popupPosition.x + offsetX,
                                y = popupPosition.y + offsetY
                            )
                        },
                    button = cardButton
                )
            }
        }
    }
}