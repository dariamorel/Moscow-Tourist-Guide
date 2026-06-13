package com.example.touristguide.presentation.ui.map_layer

import android.graphics.DashPathEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.touristguide.domain.entities.Sight
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapRouteLine(
    mapView: MapView,
    sights: List<Sight>
) {
    LaunchedEffect(sights) {
        mapView.overlays.removeAll { it is Polyline }

        if (sights.size < 2) {
            mapView.invalidate()
            return@LaunchedEffect
        }

        val polyline = Polyline(mapView).apply {
            setPoints(sights.map { it.coords })
            outlinePaint.color = android.graphics.Color.BLACK
            outlinePaint.strokeWidth = 8f
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)
        }

        mapView.overlays.add(0, polyline)
        mapView.invalidate()
    }
}
