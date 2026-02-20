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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.touristguide.data.repository.TestRepository
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.presentation.ui.ChooseSightsScreen
import com.example.touristguide.presentation.ui.MyRoutesScreen
import com.example.touristguide.presentation.ui.RouteScreen
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel
import com.example.touristguide.ui.theme.TouristGuideTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapViewModel = MapViewModel()
        val repository = TestRepository()
        val sightsViewModel = SightsViewModel(repository)

        enableEdgeToEdge()
        setContent {
            TouristGuideTheme(dynamicColor = false, darkTheme = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    MainScreen(sightsViewModel, mapViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

enum class Screens(val title: String) {
    MY_ROUTES("Мои маршруты"), CREATE_ROUTE("Создание маршрута"),
    ACTIVE_ROUTE("Активный маршрут")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    sightsViewModel: SightsViewModel,
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.MY_ROUTES.title) {
        composable(Screens.MY_ROUTES.title) {
            MyRoutesScreen(
                navController = navController,
                viewModel = sightsViewModel,
                modifier = modifier
            )
        }

        composable(Screens.CREATE_ROUTE.title) {
            ChooseSightsScreen(
                navController = navController,
                mapViewModel = mapViewModel,
                sightsViewModel = sightsViewModel,
                modifier = modifier
            )
        }

        composable(
            "${Screens.ACTIVE_ROUTE.title}/{routeId}",
            arguments = listOf(
                navArgument("routeId") {
                    type = NavType.IntType
                }
            )
            ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getInt("routeId") ?: 0
            val route = sightsViewModel.getRouteById(routeId) ?: Route()
            RouteScreen(
                route = route,
                sightsViewModel = sightsViewModel,
                modifier = modifier
            )
        }
    }
}