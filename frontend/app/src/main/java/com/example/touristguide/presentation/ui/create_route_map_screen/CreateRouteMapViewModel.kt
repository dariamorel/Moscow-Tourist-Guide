package com.example.touristguide.presentation.ui.create_route_map_screen

import android.util.Log
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

    private val _routeSights = MutableStateFlow<List<Sight>>(emptyList())
    val routeSights: StateFlow<List<Sight>> = _routeSights.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Sight>>(emptyList())
    val searchResults: StateFlow<List<Sight>> = _searchResults.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

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

    fun isSightInRoute(sightId: Int): Boolean {
        return _routeSights.value.any { it.id == sightId }
    }

    fun addSight(sight: Sight) {
        if (!isSightInRoute(sight.id)) {
            _routeSights.value = _routeSights.value + sight
        }
    }

    fun removeSight(sight: Sight) {
        _routeSights.value = _routeSights.value.filter { it.id != sight.id }
    }

    fun openSearch() {
        clearSelection()
        _isSearchOpen.value = true
        search(_searchQuery.value)
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        search(query)
    }

    fun search(query: String) {
        val allSights = _sights.value
        _searchResults.value = if (query.isBlank()) {
            allSights
        } else {
            allSights.filter { sight ->
                sight.title.contains(query, ignoreCase = true) ||
                    sight.description.contains(query, ignoreCase = true)
            }
        }
    }

    private fun loadSights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _sights.value = repository.getAllSights()
                if (_isSearchOpen.value) {
                    search(_searchQuery.value)
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while loading sights: ${e.message}.")
            }
        }
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 14.0

        const val MOSCOW_LATITUDE = 55.751244
        const val MOSCOW_LONGITUDE = 37.618423
    }
}
