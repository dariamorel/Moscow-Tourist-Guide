package com.example.touristguide.presentation.ui.go_route

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.entities.isFullyPassed
import com.example.touristguide.domain.entities.progressFraction
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoRouteViewModel(
    private val route: Route,
    private val repository: Repository
) : ViewModel() {

    val routeName: String = route.name
    val sights: List<Sight> = route.planned

    private val _isStarted = MutableStateFlow(route.status != Route.Status.NOT_STARTED)
    val isStarted: StateFlow<Boolean> = _isStarted.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPassed = MutableStateFlow(route.isPassed)
    val isPassed: StateFlow<Map<Int, Boolean>> = _isPassed.asStateFlow()

    private val _isRouteCompleted = MutableStateFlow(
        route.status == Route.Status.PASSED || route.isFullyPassed()
    )
    val isRouteCompleted: StateFlow<Boolean> = _isRouteCompleted.asStateFlow()

    private val _progress = MutableStateFlow(route.progressFraction())
    val progress: StateFlow<Float> = _progress.asStateFlow()

    init {
        refreshIsPassed()
    }

    fun isSightPassed(sightId: Int): Boolean = _isPassed.value[sightId] == true

    fun startRoute() {
        _isStarted.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentRoute = repository.getAllRoutes().find { it.id == route.id } ?: route
                if (currentRoute.status == Route.Status.NOT_STARTED) {
                    repository.updateRoute(
                        route.id,
                        currentRoute.copy(status = Route.Status.IN_PROGRESS)
                    )
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while starting route: ${e.message}.")
            }
        }
    }

    fun refreshIsPassed() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentRoute = repository.getAllRoutes().find { it.id == route.id } ?: return@launch
                _isPassed.value = currentRoute.isPassed
                updateProgress(currentRoute.isPassed)

                if (currentRoute.isFullyPassed()) {
                    _isRouteCompleted.value = true
                    _progress.value = 1f
                    if (currentRoute.status != Route.Status.PASSED) {
                        repository.updateRoute(
                            route.id,
                            currentRoute.copy(status = Route.Status.PASSED)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while refreshing route: ${e.message}.")
            }
        }
    }

    private fun updateProgress(isPassedMap: Map<Int, Boolean>) {
        val updatedRoute = route.copy(isPassed = isPassedMap)
        _progress.value = updatedRoute.progressFraction()
    }

    fun goToPrevious() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }

    fun goToNext() {
        if (_currentIndex.value < sights.lastIndex) {
            _currentIndex.value += 1
        }
    }

    fun canGoPrevious(index: Int): Boolean = index > 0

    fun canGoNext(index: Int): Boolean = index < sights.lastIndex

    fun declineRoute(onDeclined: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentRoute = repository.getAllRoutes().find { it.id == route.id } ?: route
                repository.updateRoute(
                    route.id,
                    currentRoute.copy(status = Route.Status.DECLINED)
                )
                withContext(Dispatchers.Main) {
                    onDeclined()
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while declining route: ${e.message}.")
            }
        }
    }
}
