package com.example.touristguide.presentation.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.domain.interfaces.Repository
import com.example.touristguide.presentation.ui.achievements_screen.AchievementsViewModel
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteMapViewModel
import com.example.touristguide.presentation.ui.create_route_screen.CreateRouteViewModel
import com.example.touristguide.presentation.ui.detailed_sight_screen.DetailedSightViewModel
import com.example.touristguide.presentation.ui.go_route.GoRouteViewModel
import com.example.touristguide.presentation.ui.my_routes_screen.MyRoutesViewModel
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

    fun createMyRoutesViewModel(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                MyRoutesViewModel(
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    fun createGoRouteViewModel(route: Route): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                GoRouteViewModel(
                    route = route,
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    fun createDetailedSightViewModel(routeId: Int, sight: Sight): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                DetailedSightViewModel(
                    routeId = routeId,
                    sight = sight,
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    fun createAchievementsViewModel(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                AchievementsViewModel(
                    repository = dependencies.getRepository()
                )
            }
        }
    }

    interface Dependencies {
        fun getRepository(): Repository
    }
}
