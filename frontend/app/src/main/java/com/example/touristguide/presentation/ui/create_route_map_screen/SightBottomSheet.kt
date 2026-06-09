package com.example.touristguide.presentation.ui.create_route_map_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.touristguide.domain.entities.Sight

@Composable
fun SightActionButton(
    isAdded: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isAdded) {
        OutlinedButton(
            onClick = onRemove,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Удалить",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    } else {
        Button(
            onClick = onAdd,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2B2B2B),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Добавить",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightBottomSheetContent(
    sight: Sight,
    isExpanded: Boolean,
    isAdded: Boolean,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isExpanded) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp)
            .padding(bottom = if (isExpanded) 24.dp else 16.dp)
    ) {
        Icon(
            imageVector = if (isExpanded) {
                Icons.Default.KeyboardArrowDown
            } else {
                Icons.Default.KeyboardArrowUp
            },
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 8.dp),
            tint = Color(0xFFBDBDBD)
        )

        if (isExpanded) {
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
        }

        Text(
            text = sight.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isExpanded) 22.sp else 20.sp,
                lineHeight = if (isExpanded) 28.sp else 24.sp
            ),
            color = Color.Black,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isExpanded) {
            Text(
                text = sight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        sight.subway?.takeIf { it.isNotBlank() }?.let { subway ->
            Text(
                text = "м. $subway",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (isExpanded) {
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

            Spacer(modifier = Modifier.height(8.dp))
        }

        SightActionButton(
            isAdded = isAdded,
            onAdd = onAdd,
            onRemove = onRemove
        )
    }
}
