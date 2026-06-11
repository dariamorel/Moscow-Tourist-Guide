package com.example.touristguide.presentation.ui.create_route_map_screen

import MapContainer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.touristguide.domain.entities.Sight
import com.example.touristguide.presentation.di.PresentationComponentHolder
import com.example.touristguide.presentation.ui.map_layer.MapMarkers
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouteMapScreen(
    onRouteCreate: (List<Sight>) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CreateRouteMapViewModel = viewModel(
        factory = PresentationComponentHolder.component.createCreateRouteMapViewModel()
    )

    val sights by viewModel.sights.collectAsState()
    val mapCenter by viewModel.mapCenter.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val selectedSight by viewModel.selectedSight.collectAsState()
    val routeSights by viewModel.routeSights.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()

    val routeSightIds = remember(routeSights) { routeSights.map { it.id }.toSet() }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isSheetExpanded by remember {
        derivedStateOf {
            sheetState.currentValue == SheetValue.Expanded ||
                sheetState.targetValue == SheetValue.Expanded
        }
    }

    LaunchedEffect(selectedSight, isSearchOpen) {
        if (isSearchOpen || selectedSight == null) {
            sheetState.hide()
        } else if (sheetState.currentValue == SheetValue.Hidden) {
            sheetState.partialExpand()
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .distinctUntilChanged()
            .collect { value ->
                if (value == SheetValue.Hidden && selectedSight != null && !isSearchOpen) {
                    viewModel.clearSelection()
                }
            }
    }

    if (isSearchOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSearch() },
            sheetState = searchSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White,
            dragHandle = null
        ) {
            SearchBottomSheetContent(
                query = searchQuery,
                results = searchResults,
                routeSightIds = routeSightIds,
                onQueryChange = viewModel::updateSearchQuery,
                onAddSight = viewModel::addSight,
                onRemoveSight = viewModel::removeSight
            )
        }
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 180.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = Color.White,
        sheetDragHandle = null,
        sheetSwipeEnabled = selectedSight != null,
        sheetContent = {
            selectedSight?.let { sight ->
                SightBottomSheetContent(
                    modifier = Modifier.fillMaxWidth(),
                    sight = sight,
                    isExpanded = isSheetExpanded,
                    isAdded = sight.id in routeSightIds,
                    onAdd = { viewModel.addSight(sight) },
                    onRemove = { viewModel.removeSight(sight) }
                )
            }
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            MapContainer(
                mapCenter = mapCenter,
                zoomLevel = zoomLevel,
            ) { mapView ->
                MapMarkers(
                    mapView = mapView,
                    sights = sights,
                    selectedSightId = selectedSight?.id,
                    onMapClick = {
                        viewModel.clearSelection()
                    },
                    onMarkerClick = { sight ->
                        viewModel.selectSight(sight)
                    }
                )
            }

            if (!isSearchOpen) {
                MapSearchBar(
                    onClick = { viewModel.openSearch() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                CreateRouteButton(
                    onClick = { onRouteCreate(routeSights) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}
