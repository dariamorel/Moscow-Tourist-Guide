package com.example.touristguide.presentation.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.interfaces.Repository
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteMapViewModel
import com.example.touristguide.presentation.ui.create_route_screen.CreateRouteViewModel
import com.example.touristguide.presentation.ui.optimizing_loading_screen.OptimizingLoadingViewModel

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

    fun createCreateRouteViewModel(optimizedOrder: OptimizedSightsOrder): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                CreateRouteViewModel(
                    optimizedSightsOrder = optimizedOrder,
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    fun createOptimizingLoadingViewModel(sightIds: List<Int>): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                OptimizingLoadingViewModel(
                    sightIds = sightIds,
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    interface Dependencies {
        fun getRepository(): Repository
    }
}
