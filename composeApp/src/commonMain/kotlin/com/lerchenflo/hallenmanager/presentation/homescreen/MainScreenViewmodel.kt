package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.withCornerPointsAtOrigin
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenAction.*
import com.lerchenflo.hallenmanager.presentation.homescreen.search.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.collections.filter

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewmodel(
    private val areaRepository: AreaRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


    private val _selectedAreaId = MutableStateFlow(0L)
    private val _searchterm = MutableStateFlow("")

    private var _offset = MutableStateFlow(Offset.Zero)
    private var _scale = MutableStateFlow(1f)
    private var _viewportsize = MutableStateFlow(IntSize(1000,1000))

    private var _currentArea = MutableStateFlow<Area?>(null)

    init {
        viewModelScope.launch {

            //Load first area from db
            loadDefaultArea()


            viewModelScope.launch {
                _selectedAreaId
                    .filter { it > 0L }
                    .flatMapLatest { id -> areaRepository.getAreaByIdFlow(id) }
                    .flowOn(Dispatchers.IO)
                    .collectLatest { area ->

                        _currentArea.value = area
                        recalculateVisibleItems()
                    }
            }

            viewModelScope.launch {
                areaRepository.getAllLayers()
                    .flowOn(Dispatchers.IO)
                    .collectLatest { layers ->
                    state = state.copy(
                        availableLayers = layers
                    )
                }
            }

            //Available areas dropdown
            viewModelScope.launch {
                areaRepository.getAreas()
                    .map { areas ->
                        areas.map {
                            AvailableArea(
                                id = it.id,
                                name = it.name,
                                description = it.description
                            )
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .distinctUntilChanged()
                    .collectLatest { availableareas ->
                        state = state.copy(
                            availableAreas = availableareas
                        )
                    }
            }

            //Item search
            viewModelScope.launch {
                areaRepository.getAllItems()
                    .combine(_searchterm.asStateFlow()) { items, searchTerm ->
                        if (searchTerm.isBlank()) {
                            emptyList()
                        } else {
                            val query = searchTerm.trim().lowercase()
                            items.filter { item ->
                                item.matchesSearchQuery(query)
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect { searchResults ->
                        state = state.copy(
                            currentSearchResult = searchResults.map {
                                SearchItem(
                                    item = it,
                                    areaname = state.availableAreas.find { area ->
                                        area.id == it.areaId
                                    }?.name ?: "" //Should not happen
                                )
                            }
                        )
                    }
            }
        }
    }

    private fun loadDefaultArea(){
        CoroutineScope(Dispatchers.IO).launch {
            val defaultarea = areaRepository.getFirstArea()

            state = state.copy(
                currentArea = defaultarea
            )

            _selectedAreaId.value = defaultarea?.id ?: 0L
        }
    }


    fun onAction(action: MainScreenAction) {
        when (action) {
            is OnSearchtermChange -> {
                //Searchterm not empty -> User is searching
                _searchterm.value = action.newsearchTerm

                state = state.copy(
                    searchterm = action.newsearchTerm,
                    //Searchquery is handled in with the flow
                )
            }


            is OnAddPoint -> {
                val cornerpoints = state.currentDrawingOffsets + action.offset

                val finished = cornerpoints.first() == cornerpoints.last() && cornerpoints.size >= 2
                state = state.copy(
                    currentDrawingOffsets = cornerpoints,
                    iteminfopopupshown = finished,//Finished drawing
                    isDrawing = !finished,
                    iteminfopopupItem = null,
                )
            }

            is OnStopPainting -> {
                state = state.copy(
                    isDrawing = false,
                    currentDrawingOffsets = emptyList()
                )
            }

            OnInfoDialogDismiss -> {
                state = state.copy(
                    iteminfopopupshown = false,
                    isDrawing = false,
                    currentDrawingOffsets = emptyList()
                )
            }

            is OnInfoDialogSave -> {
                state = state.copy(
                    iteminfopopupshown = false,
                    currentDrawingOffsets = emptyList(),
                )

                //Area is selected (Should always be)
                state.currentArea.let { area ->
                    if (area != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            areaRepository.upsertItem(action.item, area.id)
                        }
                    } else {
                        throw IllegalStateException("Items can not be upserted, no Area is selected!")
                    }

                }


            }

            is OnSelectArea -> {
                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        val area = areaRepository.getAreaById(action.areaid)

                        state = state.copy(
                            currentArea = area
                        )

                        _selectedAreaId.value = area!!.id
                    }
                }
            }

            is OnAreaDialogSave -> {
                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentarea = areaRepository.upsertArea(action.area)

                        state = state.copy(
                            currentArea = currentarea,
                            areainfopopupshown = false
                        )

                        _selectedAreaId.value = currentarea.id
                    }
                }
            }

            OnCreateAreaStart -> {
                state = state.copy(
                    areainfopopupshown = true
                )
            }

            OnAreaDialogDismiss -> {
                state = state.copy(
                    areainfopopupshown = false
                )
            }

            OnLayersClicked -> {
                viewModelScope.launch {
                    navigator.navigate(Route.Layers)
                }
            }

            is OnZoom -> {

                _scale.value = action.scale
                _offset.value = action.offset
                _viewportsize.value = action.viewportsize


                val newzoom = action.scale

                val newgridspacing = when {
                    newzoom < 0.15f -> {
                        800f
                    }

                    newzoom < 0.5f -> {
                        400f
                    }

                    newzoom < 0.8f -> {
                        200f
                    }

                    newzoom < 1.5f -> {
                        100f
                    }

                    newzoom < 2.5f -> {
                        50f
                    }

                    else -> {
                        25f
                    }
                }


                state = state.copy(
                    gridspacing = newgridspacing,
                )


                recalculateVisibleItems()
            }

            is OnItemClicked -> {
                state = state.copy(
                    iteminfopopupshown = true,
                    iteminfopopupItem = action.item
                )
            }

            is OnClick -> {
                if (state.isDrawing){
                    onAction(OnAddPoint(action.contentpoint))
                }else{
                    if (action.longpressed){
                        //If not drawing and longpress show context menu
                        //TODO: Show context menu
                    }else{
                        //If not drawing and sort press show popup
                        state.currentArea?.let { area ->
                            for (item in area.items.filter { it.onArea }) {
                                //println(item)

                                if (isPointInPolygon(action.contentpoint, item.cornerPoints)) {
                                    //println("Not clicked")

                                    onAction(OnItemClicked(item))
                                    break
                                }
                            }
                        }
                    }
                }
            }

            OnStartPainting -> {
                state = state.copy(
                    isDrawing = true
                )
            }

            is OnShowShortAccessMenuClick -> {
                state = state.copy(
                    showShortAccessMenu = action.shown
                )
            }

            is OnMoveItemToGrid -> {
                viewModelScope.launch {
                    areaRepository.upsertItem(action.item
                        .copy(
                            onArea = true,
                            cornerPoints = action.item.cornerPoints.map { point ->
                                point + action.position
                            }
                        ), _selectedAreaId.value)
                }
            }

            is OnMoveItemToShortAccess -> {
                viewModelScope.launch {
                    //Upsert item but moved to the coordinate startpoint
                    areaRepository.upsertItem(action.item.withCornerPointsAtOrigin().copy(
                        onArea = false,
                    ), _selectedAreaId.value)

                    state = state.copy(
                        iteminfopopupshown = false,
                        iteminfopopupItem = null
                    )
                }
            }
        }
    }

    private fun recalculateVisibleItems(){
        val filtereditems = _currentArea.value?.items?.filter { item ->
            val minX = item.cornerPoints.minOf { it.x } * _scale.value + _offset.value.x
            val maxX = item.cornerPoints.maxOf { it.x } * _scale.value + _offset.value.x
            val minY = item.cornerPoints.minOf { it.y } * _scale.value + _offset.value.y
            val maxY = item.cornerPoints.maxOf { it.y } * _scale.value + _offset.value.y

            // Check if bounding box intersects viewport
            (maxX >= 0 && minX <= _viewportsize.value.width && maxY >= 0 && minY <= _viewportsize.value.height) || !item.onArea //Keep invisible items
        }?.sortedBy { it.getPriority() }
            ?.filter {
                it.isVisible() || !it.onArea //Keep invisible items
            }

        //println(filtereditems?.filter { !it.onArea }?.size)

        val newarea = _currentArea.value?.copy(
            items = filtereditems?: emptyList()
        )

        state = state.copy(
            currentArea = newarea
        )
    }




}