package com.example.touristguide.presentation.ui.detailed_sight_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailedSightViewModel(
    private val routeId: Int,
    private val sight: Sight,
    private val repository: Repository
) : ViewModel() {

    private val _isPassed = MutableStateFlow(false)
    val isPassed: StateFlow<Boolean> = _isPassed.asStateFlow()

    init {
        loadPassedState()
    }

    private fun loadPassedState() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val route = repository.getAllRoutes().find { it.id == routeId }
                _isPassed.value = route?.isPassed?.get(sight.id) == true
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while loading sight state: ${e.message}.")
            }
        }
    }

    fun markAsPassed(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentRoute = repository.getAllRoutes().find { it.id == routeId } ?: return@launch
                val updatedPassed = currentRoute.isPassed.toMutableMap().apply {
                    put(sight.id, true)
                }
                repository.updateRoute(
                    routeId,
                    currentRoute.copy(isPassed = updatedPassed)
                )
                _isPassed.value = true
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while marking sight passed: ${e.message}.")
            }
        }
    }
}
