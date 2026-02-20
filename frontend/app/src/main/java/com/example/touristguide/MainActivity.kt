package com.example.touristguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.touristguide.data.repository.TestRepository
import com.example.touristguide.presentation.ui.ChooseSightsScreen
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel
import com.example.touristguide.ui.theme.TouristGuideTheme

class MainActivity : ComponentActivity() {
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
                    ChooseSightsScreen(mapViewModel, sightsViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
