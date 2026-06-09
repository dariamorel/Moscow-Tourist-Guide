package com.example.touristguide.presentation.ui.create_route_map_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateRouteMapViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _mapCenter = MutableStateFlow(
        GeoPoint(MOSCOW_LATITUDE, MOSCOW_LONGITUDE)
    )
    val mapCenter: StateFlow<GeoPoint> = _mapCenter.asStateFlow()

    private val _zoomLevel = MutableStateFlow(DEFAULT_ZOOM_LEVEL)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    private val _sights = MutableStateFlow<List<Sight>>(emptyList())
    val sights: StateFlow<List<Sight>> = _sights.asStateFlow()

    private val _selectedSight = MutableStateFlow<Sight?>(null)
    val selectedSight: StateFlow<Sight?> = _selectedSight.asStateFlow()

    init {
        loadSights()
    }

    fun updateMapCenter(center: GeoPoint) {
        _mapCenter.value = center
    }

    fun updateZoom(zoom: Double) {
        _zoomLevel.value = zoom
    }

    fun selectSight(sight: Sight) {
        _selectedSight.value = sight
    }

    fun clearSelection() {
        _selectedSight.value = null
    }

    private fun loadSights() {
        viewModelScope.launch(Dispatchers.IO) {
            _sights.value = repository.getAllSights()
        }
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 14.0

        const val MOSCOW_LATITUDE = 55.751244
        const val MOSCOW_LONGITUDE = 37.618423
    }
}