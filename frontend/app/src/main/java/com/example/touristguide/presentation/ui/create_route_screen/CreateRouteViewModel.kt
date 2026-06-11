package com.example.touristguide.presentation.ui.create_route_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateRouteViewModel(
    optimizedSightsOrder: OptimizedSightsOrder,
    private val repository: Repository,
) : ViewModel() {

    private val _optimizedSightsOrder = MutableStateFlow(optimizedSightsOrder)
    val optimizedSightsOrder: StateFlow<OptimizedSightsOrder> = _optimizedSightsOrder.asStateFlow()

    private val _routeName = MutableStateFlow("Маршрут")
    val routeName: StateFlow<String> = _routeName.asStateFlow()

    fun updateRouteName(name: String) {
        _routeName.value = name
    }

    fun createRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addNewRoute(_optimizedSightsOrder.value.sights)
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while creating route: ${e.message}.")
            }
        }
    }
}
