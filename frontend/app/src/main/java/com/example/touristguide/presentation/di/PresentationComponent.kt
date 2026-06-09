package com.example.touristguide.presentation.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.touristguide.domain.interfaces.Repository
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteMapViewModel

class PresentationComponent(
    private val dependencies: Dependencies
) {
    fun createCreateRouteMapViewModel(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                CreateRouteMapViewModel(
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    interface Dependencies {
        fun getRepository(): Repository
    }
}