package com.example.touristguide.presentation.components

import MapItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.touristguide.MapViewModel

@Composable
fun SightsMapScreen(
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .size(width = 500.dp, height = 600.dp)
            .clip(RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        MapItem(mapViewModel, Modifier.fillMaxSize())
    }
}