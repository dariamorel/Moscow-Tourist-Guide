package com.example.touristguide

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

class MapViewModel: ViewModel() {
    private val _mapCenter = MutableStateFlow(GeoPoint(MOSCOW_LATITUDE, MOSCOW_LONGITUDE))
    val mapCenter: StateFlow<GeoPoint> = _mapCenter.asStateFlow()

    private val _zoomLevel = MutableStateFlow(DEFAULT_ZOOM_LEVEL)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    fun updateMapCenter(newCenter: GeoPoint) {
        _mapCenter.value = newCenter
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15.0
        const val MOSCOW_LATITUDE = 55.751244
        const val MOSCOW_LONGITUDE = 37.618423
    }
}