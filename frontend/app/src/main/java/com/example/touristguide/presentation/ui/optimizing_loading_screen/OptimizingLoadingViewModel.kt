package com.example.touristguide.presentation.ui.optimizing_loading_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OptimizingLoadingViewModel(
    private val repository: Repository,
    sightIds: List<Int>
): ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _optimizedSights = MutableStateFlow<OptimizedSightsOrder?>(null)
    val optimizedSights = _optimizedSights.asStateFlow()

    init {
        optimize(sightIds)
    }

    fun optimize(sightIds: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _optimizedSights.value = repository.optimizeRoute(sightIds)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while optimizing sights: ${e.message}.")
                _isLoading.value = false
            }
        }
    }
}