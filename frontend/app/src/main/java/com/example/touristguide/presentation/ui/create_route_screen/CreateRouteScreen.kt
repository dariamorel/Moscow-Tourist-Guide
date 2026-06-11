package com.example.touristguide.presentation.ui.create_route_screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.OptimizedSightsOrder
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.di.PresentationComponentHolder

@Composable
fun CreateRouteScreen(
    optimizedSightsOrder: OptimizedSightsOrder,
    modifier: Modifier = Modifier,
    onRouteCreate: () -> Unit
) {
    val viewModel: CreateRouteViewModel = viewModel(
        factory = PresentationComponentHolder.component.createCreateRouteViewModel(optimizedSightsOrder)
    )

    val order by viewModel.optimizedSightsOrder.collectAsState()
    val routeName by viewModel.routeName.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        OutlinedTextField(
            value = routeName,
            onValueChange = viewModel::updateRouteName,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF757575)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(order.sights) { index, sight ->
                RouteSightItem(
                    number = index + 1,
                    sight = sight,
                    isLast = index == order.sights.lastIndex
                )
            }
        }

        Button(
            onClick = {
                viewModel.createRoute(routeName)
                onRouteCreate()
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2B2B2B),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Создать маршрут",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }

        Text(
            text = "Маршрут уже оптимизирован по времени обхода.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575)
        )

        Text(
            text = "Оптимизированное время: ${order.totalTime}",
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575)
        )
    }
}

@Composable
private fun RouteSightItem(
    number: Int,
    sight: Sight,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575)
            )

            if (!isLast) {
                DottedVerticalLine(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(48.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = sight.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            sight.address?.takeIf { it.isNotBlank() }?.let { address ->
                Text(
                    text = address,
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun DottedVerticalLine(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.width(2.dp)) {
        drawLine(
            color = Color(0xFFBDBDBD),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }
}
