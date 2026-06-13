package com.example.touristguide.presentation.ui.detailed_sight_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.common.ScreenBackButton

@Composable
fun DetailedSightScreen(
    routeId: Int,
    sight: Sight,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: DetailedSightViewModel = viewModel(
        factory = PresentationComponentHolder.component.createDetailedSightViewModel(routeId, sight)
    )

    val isPassed by viewModel.isPassed.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ScreenBackButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }

        item {
            AsyncImage(
                model = sight.image,
                contentDescription = sight.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                sight.subway?.takeIf { it.isNotBlank() }?.let { subway ->
                    DetailInfoText("м. $subway")
                }

                sight.address?.takeIf { it.isNotBlank() }?.let { address ->
                    DetailInfoText(address)
                }

                sight.timetable?.takeIf { it.isNotBlank() }?.let { timetable ->
                    DetailInfoText(timetable)
                }

                sight.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    DetailInfoText("тел.: $phone")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = sight.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = sight.bodyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (sight.categories.isNotEmpty()) {
                    Text(
                        text = "Категории: ${sight.categories.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!isPassed) {
            item {
                Surface(
                    onClick = { viewModel.markAsPassed(onBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFC8E6C9)
                ) {
                    Text(
                        text = "Отметить пройденным",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 4.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF757575),
        textAlign = TextAlign.Center
    )
}
