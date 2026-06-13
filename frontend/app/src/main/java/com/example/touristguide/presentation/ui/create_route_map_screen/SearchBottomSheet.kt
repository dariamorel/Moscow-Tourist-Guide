package com.example.touristguide.presentation.ui.create_route_map_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.touristguide.domain.entities.Sight

@Composable
fun SearchBottomSheetContent(
    query: String,
    results: List<Sight>,
    routeSightIds: Set<Int>,
    onQueryChange: (String) -> Unit,
    onAddSight: (Sight) -> Unit,
    onRemoveSight: (Sight) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 8.dp),
            tint = Color(0xFFBDBDBD)
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = {
                Text("Поиск места", color = Color(0xFF9E9E9E))
            },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
            },
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = Color(0xFFE0E0E0)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(results, key = { it.id }) { sight ->
                SightSearchCard(
                    sight = sight,
                    isAdded = sight.id in routeSightIds,
                    onAdd = { onAddSight(sight) },
                    onRemove = { onRemoveSight(sight) }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
            }
        }
    }
}

@Composable
private fun SightSearchCard(
    sight: Sight,
    isAdded: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        AsyncImage(
            model = sight.image,
            contentDescription = sight.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = sight.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = sight.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        sight.subway?.takeIf { it.isNotBlank() }?.let { subway ->
            Text(
                text = "м. $subway",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        sight.timetable?.takeIf { it.isNotBlank() }?.let { timetable ->
            Text(
                text = timetable,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        sight.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            Text(
                text = "тел.: $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        SightCategoriesText(categories = sight.categories)

        Spacer(modifier = Modifier.height(12.dp))

        SightActionButton(
            isAdded = isAdded,
            onAdd = onAdd,
            onRemove = onRemove
        )
    }
}
