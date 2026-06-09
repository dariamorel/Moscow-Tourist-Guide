package com.example.touristguide.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RouteViewModel(
    private val repository: Repository
): ViewModel() {
    val allRoutes = repository.allRoutes.map { routes ->
        routes.sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun getRouteById(id: Int) = allRoutes.value.find { route -> route.id == id }

    fun changeRouteStatus(newStatus: Route.Status, route: Route) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRoute(
                route.id, route.copy(
                    status = newStatus
                )
            )
        }
    }

    fun changeSightStatus(newStatus: Boolean, sightIdx: Int, route: Route) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRoute(
                route.id, route.copy(
                    isPassed = route.isPassed.toMutableMap().apply { this[sightIdx] = newStatus }
                )
            )
        }
    }
}