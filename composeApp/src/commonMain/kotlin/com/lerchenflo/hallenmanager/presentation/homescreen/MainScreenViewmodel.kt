package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.data.database.AreaRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewmodel(
    private val areaRepository: AreaRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


    private val _selectedAreaId = MutableStateFlow(0L)
    private val _searchterm = MutableStateFlow("")



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
                        state = state.copy(
                            currentArea = area
                        )
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
            is MainScreenAction.OnSearchtermChange -> {
                //Searchterm not empty -> User is searching
                _searchterm.value = action.newsearchTerm

                state = state.copy(
                    searchterm = action.newsearchTerm,
                    //Searchquery is handled in with the flow
                )
            }


            is MainScreenAction.OnAddPoint -> {
                val cornerpoints = state.currentDrawingOffsets + action.offset

                val finished = cornerpoints.first() == cornerpoints.last() && cornerpoints.size >= 2
                state = state.copy(
                    currentDrawingOffsets = cornerpoints,
                    iteminfopopupshown = finished,//Finished drawing
                    isDrawing = !finished,
                    iteminfopopupItem = null,
                )
            }

            is MainScreenAction.OnStopPainting -> {
                state = state.copy(
                    isDrawing = false,
                    currentDrawingOffsets = emptyList()
                )
            }

            MainScreenAction.OnInfoDialogDismiss -> {
                state = state.copy(
                    iteminfopopupshown = false,
                    isDrawing = false,
                    currentDrawingOffsets = emptyList()
                )
            }

            is MainScreenAction.OnInfoDialogSave -> {
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

            is MainScreenAction.OnSelectArea -> {
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

            is MainScreenAction.OnAreaDialogSave -> {
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

            MainScreenAction.OnCreateAreaStart -> {
                state = state.copy(
                    areainfopopupshown = true
                )
            }

            MainScreenAction.OnAreaDialogDismiss -> {
                state = state.copy(
                    areainfopopupshown = false
                )
            }

            MainScreenAction.OnSettingsClicked -> {
                viewModelScope.launch {
                    navigator.navigate(Route.Settings)
                }
            }

            is MainScreenAction.OnZoom -> {
                println("Zoomlevel: ${action.newzoomlevel}")

                val newzoom = action.newzoomlevel

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
                    gridspacing = newgridspacing
                )
            }

            is MainScreenAction.OnItemClicked -> {
                state = state.copy(
                    iteminfopopupshown = true,
                    iteminfopopupItem = action.item
                )
            }
        }
    }




}