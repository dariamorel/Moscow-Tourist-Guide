package com.example.touristguide.presentation.ui.route_map_screen

import androidx.lifecycle.ViewModel
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import org.osmdroid.util.GeoPoint
import kotlin.math.max

class RouteMapViewModel(
    route: Route
) : ViewModel() {

    val routeName: String = route.name
    val sights: List<Sight> = route.planned
    val mapCenter: GeoPoint = calculateCenter(sights)
    val zoomLevel: Double = calculateZoom(sights)

    private fun calculateCenter(sights: List<Sight>): GeoPoint {
        if (sights.isEmpty()) {
            return GeoPoint(MOSCOW_LATITUDE, MOSCOW_LONGITUDE)
        }
        val latitude = sights.map { it.coords.latitude }.average()
        val longitude = sights.map { it.coords.longitude }.average()
        return GeoPoint(latitude, longitude)
    }

    private fun calculateZoom(sights: List<Sight>): Double {
        if (sights.size <= 1) return DEFAULT_ZOOM_LEVEL

        val latSpan = sights.maxOf { it.coords.latitude } - sights.minOf { it.coords.latitude }
        val lonSpan = sights.maxOf { it.coords.longitude } - sights.minOf { it.coords.longitude }
        val span = max(latSpan, lonSpan)

        return when {
            span > 0.2 -> 11.0
            span > 0.1 -> 12.0
            span > 0.05 -> 13.0
            else -> DEFAULT_ZOOM_LEVEL
        }
    }

    companion object {
        private const val MOSCOW_LATITUDE = 55.7558
        private const val MOSCOW_LONGITUDE = 37.6173
        private const val DEFAULT_ZOOM_LEVEL = 14.0
    }
}
