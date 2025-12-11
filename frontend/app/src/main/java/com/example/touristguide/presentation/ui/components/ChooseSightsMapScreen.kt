package com.example.touristguide.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.touristguide.presentation.viewModel.MapViewModel
import com.example.touristguide.presentation.viewModel.SightsViewModel

@Composable
fun ChooseSightsMapScreen(
    mapViewModel: MapViewModel,
    sightsViewModel: SightsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Создание маршрута",
            fontSize = 20.sp,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleMedium
        )
        SightsMapItem(
            mapViewModel,
            sightsViewModel,
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
        ) @Composable { modifier ->
            var buttonText by remember { mutableStateOf("Добавить") }

            Button(
                onClick = {
                    when (buttonText) {
                        "Добавить" -> buttonText = "Удалить"
                        "Удалить" -> buttonText = "Добавить"
                    }
                },
                modifier = modifier,
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Button(
            onClick = {},
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Создать маршрут",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}