package com.example.touristguide.presentation.viewModel

import android.util.Log
import androidx.compose.runtime.DisposableEffect
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

    private val _querySights = MutableStateFlow<List<Sight>>(listOf())
    val querySights = _querySights.asStateFlow()

    private val _selectedSight = MutableStateFlow<Sight?>(null)
    val selectedSight: StateFlow<Sight?> = _selectedSight.asStateFlow()

    private val _categoriesList = MutableStateFlow<List<String>>(listOf())
    val categoriesList = _categoriesList.asStateFlow()

    init {
        getAllSights()
        getCategoriesList()
    }

    private fun getAllSights() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _allSights.value = repository.getAllSights()
                _querySights.value = _allSights.value
            } catch(e: Exception) {
                Log.e(SIGHTS_TAG, "Error getting all sights: ${e.message}")
            }
        }
    }

    fun searchSights(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _querySights.value = _allSights.value.filter { sight ->
                    sight.title.contains(query, ignoreCase = true) ||
                            sight.description.contains(query, ignoreCase = true) ||
                            sight.bodyText.contains(query, ignoreCase = true)
                }
            } catch (e: Exception) {
                Log.e(SIGHTS_TAG, "Error while searching sights: ${e.message}.")
            }
        }
    }

    fun searchSightByCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _querySights.value = _allSights.value.filter { sight ->
                    sight.categories.contains(category.replaceFirstChar { it.lowercase() })
                }
            } catch (e: Exception) {
                Log.e(SIGHTS_TAG, "Error while searching sights: ${e.message}.")
            }
        }
    }

    private fun getCategoriesList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _categoriesList.value = repository.getCategoriesList()
            } catch (e: Exception) {
                Log.e(SIGHTS_TAG, "Error while getting categories: ${e.message}.")
            }
        }
    }

    fun onSightSelected(sight: Sight) {
        _selectedSight.value = sight
    }

    fun onDismissSightDetail() {
        _selectedSight.value = null
    }

    companion object {
        const val SIGHTS_TAG = "Sights"
    }
}