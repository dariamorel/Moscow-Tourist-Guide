package com.example.touristguide.presentation.ui.profile_screen

import androidx.lifecycle.ViewModel
import com.example.touristguide.domain.interfaces.Repository

class ProfileViewModel(
    private val repository: Repository
) : ViewModel() {

    val login: String
        get() = repository.getLogin().orEmpty()

    val userId: Int
        get() = repository.getUserId() ?: 0

    fun logout() {
        repository.logout()
    }
}
