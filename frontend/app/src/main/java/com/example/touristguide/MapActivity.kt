package com.example.touristguide

import MapScreen
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent

class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapViewModel = MapViewModel()
        enableEdgeToEdge()
        setContent {
            MapScreen(mapViewModel)
        }
    }
}
