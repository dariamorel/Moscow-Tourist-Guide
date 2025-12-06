package com.example.touristguide.presentation.viewModel

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

class SightsViewModel(
    private val repository: Repository
): ViewModel() {
    private val _allSights = MutableStateFlow<List<Sight>>(emptyList())
    val allSights: StateFlow<List<Sight>> = _allSights.asStateFlow()

    init {
        getAllSights()
    }

    fun getAllSights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allSights.value = repository.getAllSights()
            } catch(e: Exception) {
                Log.e(SIGHTS_TAG, "Error getting all sights: ${e.message}")
            }
        }
    }

    fun onSightSelected(sight: Sight) {
    }

    companion object {
        const val SIGHTS_TAG = "Sights"
    }
}