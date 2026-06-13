package com.example.touristguide.presentation.ui.my_routes_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyRoutesViewModel(
    private val repository: Repository
): ViewModel() {
    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes = _routes.asStateFlow()

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _routes.value = repository.getAllRoutes()
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while creating route: ${e.message}.")
            }
        }
    }
}