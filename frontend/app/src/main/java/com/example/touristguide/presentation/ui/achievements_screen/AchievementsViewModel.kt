package com.example.touristguide.presentation.ui.achievements_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.interfaces.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _achievements.value = repository.getAllAchievements()
            } catch (e: Exception) {
                Log.e(Repository.REPOSITORY_TAG, "Error while getting achievements: ${e.message}.")
            }
        }
    }
}
