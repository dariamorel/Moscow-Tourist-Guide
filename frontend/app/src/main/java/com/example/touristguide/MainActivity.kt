package com.example.touristguide

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.touristguide.data.di.DataComponentHolder
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.achievements_screen.AchievementsScreen
import com.example.touristguide.presentation.ui.authorization_screen.AuthorizationViewModel
import com.example.touristguide.presentation.ui.authorization_screen.LoginScreen
import com.example.touristguide.presentation.ui.authorization_screen.RegisterScreen
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteMapScreen
import com.example.touristguide.presentation.ui.create_route_screen.CreateRouteScreen
import com.example.touristguide.presentation.ui.detailed_sight_screen.DetailedSightScreen
import com.example.touristguide.presentation.ui.go_route.GoRouteScreen
import com.example.touristguide.presentation.ui.my_routes_screen.MyRoutesScreen
import com.example.touristguide.presentation.ui.optimizing_loading_screen.OptimizingLoadingScreen
import com.example.touristguide.presentation.ui.profile_screen.ProfileScreen
import com.example.touristguide.presentation.ui.route_map_screen.RouteMapScreen
import com.example.touristguide.ui.theme.TouristGuideTheme
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TouristGuideTheme(dynamicColor = false, darkTheme = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

enum class Screens {
    LOGIN, REGISTER, MY_ROUTES, PROFILE, CREATE_ROUTE_MAP, OPTIMIZING_LOADING,
    CREATE_ROUTE, GO_ROUTE, ROUTE_MAP, DETAILED_SIGHT, ACHIEVEMENTS
}

private const val AUTH_GRAPH = "auth"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val repository = remember { DataComponentHolder.component.repository }
    val startDestination = remember {
        if (repository.isLogin()) Screens.MY_ROUTES.name else AUTH_GRAPH
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val canNavigateBack = currentRoute != Screens.MY_ROUTES.name &&
        currentRoute != Screens.LOGIN.name

    BackHandler(enabled = canNavigateBack) {
        navController.popBackStack()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigation(
            route = AUTH_GRAPH,
            startDestination = Screens.LOGIN.name
        ) {
            composable(Screens.LOGIN.name) { entry ->
                val authViewModel: AuthorizationViewModel = viewModel(
                    viewModelStoreOwner = remember(entry) {
                        navController.getBackStackEntry(AUTH_GRAPH)
                    },
                    factory = PresentationComponentHolder.component.createAuthorizationViewModel()
                )

                LoginScreen(
                    viewModel = authViewModel,
                    modifier = modifier,
                    onLoginSuccess = {
                        navController.navigate(Screens.MY_ROUTES.name) {
                            popUpTo(AUTH_GRAPH) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screens.REGISTER.name)
                    }
                )
            }

            composable(Screens.REGISTER.name) { entry ->
                val authViewModel: AuthorizationViewModel = viewModel(
                    viewModelStoreOwner = remember(entry) {
                        navController.getBackStackEntry(AUTH_GRAPH)
                    },
                    factory = PresentationComponentHolder.component.createAuthorizationViewModel()
                )

                RegisterScreen(
                    viewModel = authViewModel,
                    modifier = modifier,
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screens.MY_ROUTES.name) {
                            popUpTo(AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screens.MY_ROUTES.name) {
            MyRoutesScreen(
                modifier = modifier,
                onCreateRouteClick = {
                    navController.navigate(Screens.CREATE_ROUTE_MAP.name)
                },
                onRouteClick = { route ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("route", route)
                    navController.navigate(Screens.GO_ROUTE.name)
                },
                onAchievementsClick = {
                    navController.navigate(Screens.ACHIEVEMENTS.name)
                },
                onProfileClick = {
                    navController.navigate(Screens.PROFILE.name)
                }
            )
        }

        composable(Screens.PROFILE.name) {
            ProfileScreen(
                modifier = modifier,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(AUTH_GRAPH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screens.CREATE_ROUTE_MAP.name) {
            CreateRouteMapScreen(
                modifier = modifier,
                onBack = { navController.popBackStack() },
                onRouteCreate = { sights ->
                    val sightIds = sights.joinToString(",") { it.id.toString() }
                    navController.navigate("${Screens.OPTIMIZING_LOADING.name}/${sightIds}")
                }
            )
        }

        composable(
            route = "${Screens.OPTIMIZING_LOADING.name}/{sightIds}",
            arguments = listOf(navArgument("sightIds") { type = NavType.StringType })
        ) { backStackEntity ->
            val idsString = backStackEntity.arguments?.getString("sightIds") ?: ""
            val ids = idsString.split(",").mapNotNull { it.toIntOrNull() }
            OptimizingLoadingScreen(
                sightIds = ids,
                modifier = modifier,
                onBack = { navController.popBackStack() }
            ) { optimizedSightsOrder ->
                navController.getBackStackEntry(Screens.CREATE_ROUTE_MAP.name)
                    .savedStateHandle["optimizedOrder"] = optimizedSightsOrder
                navController.navigate(Screens.CREATE_ROUTE.name) {
                    popUpTo(Screens.CREATE_ROUTE_MAP.name) { inclusive = false }
                }
            }
        }

        composable(Screens.CREATE_ROUTE.name) {
            val optimizedOrder = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<OptimizedSightsOrder>("optimizedOrder")
                ?: OptimizedSightsOrder()
            CreateRouteScreen(
                optimizedSightsOrder = optimizedOrder,
                modifier = modifier,
                onBack = { navController.popBackStack() },
                onRouteCreate = {
                    navController.popBackStack(Screens.MY_ROUTES.name, inclusive = false)
                }
            )
        }

        composable(Screens.ACHIEVEMENTS.name) {
            AchievementsScreen(
                modifier = modifier,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screens.GO_ROUTE.name) {
            val route = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Route>("route")
            route?.let {
                GoRouteScreen(
                    route = route,
                    modifier = modifier,
                    onClose = { navController.popBackStack() },
                    onOpenSight = { sight ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("routeId", route.id)
                        navController.currentBackStackEntry?.savedStateHandle?.set("sight", sight)
                        navController.navigate(Screens.DETAILED_SIGHT.name)
                    },
                    onOpenMap = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("route", route)
                        navController.navigate(Screens.ROUTE_MAP.name)
                    },
                    onNavigateToMain = {
                        navController.popBackStack(Screens.MY_ROUTES.name, inclusive = false)
                    }
                )
            }
        }

        composable(Screens.ROUTE_MAP.name) {
            val route = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Route>("route")
            route?.let {
                RouteMapScreen(
                    route = route,
                    modifier = modifier,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screens.DETAILED_SIGHT.name) {
            val routeId = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Int>("routeId")
            val sight = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Sight>("sight")

            if (routeId != null && sight != null) {
                DetailedSightScreen(
                    routeId = routeId,
                    sight = sight,
                    modifier = modifier,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
