package com.example.touristguide

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.presentation.ui.create_route_map_screen.CreateRouteMapScreen
import com.example.touristguide.presentation.ui.create_route_screen.CreateRouteScreen
import com.example.touristguide.presentation.ui.optimizing_loading_screen.OptimizingLoadingScreen
import com.example.touristguide.ui.theme.TouristGuideTheme

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
    CREATE_ROUTE_MAP, OPTIMIZING_LOADING, CREATE_ROUTE
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.CREATE_ROUTE_MAP.name) {
        composable(Screens.CREATE_ROUTE_MAP.name) {
            CreateRouteMapScreen(
                modifier = modifier,
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
                modifier = modifier
            ) { optimizedSightsOrder ->
                navController.currentBackStackEntry?.savedStateHandle?.set("optimizedOrder", optimizedSightsOrder)
                navController.navigate(Screens.CREATE_ROUTE.name)
            }
        }

        composable(Screens.CREATE_ROUTE.name) {
            val optimizedOrder = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<OptimizedSightsOrder>("optimizedOrder")
                ?: OptimizedSightsOrder()
            CreateRouteScreen(
                optimizedSightsOrder = optimizedOrder,
                modifier = modifier
            )
        }
    }
}