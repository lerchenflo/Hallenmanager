package com.lerchenflo.hallenmanager.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.domain.toItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewmodel(
    private val areaRepository: AreaRepository
): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


    private val _selectedAreaId = MutableStateFlow(0L)


    init {
        viewModelScope.launch {

            //Load first area from db
            loadDefaultArea()



            viewModelScope.launch {
                _selectedAreaId
                    .filterNotNull()
                    .flatMapLatest { id -> areaRepository.getAreaByIdFlow(id) }
                    .collect { area ->
                        state = state.copy(currentArea = area)
                    }
            }

            viewModelScope.launch {
                areaRepository.getAreas()
                    .map { areas -> areas.map { it.name } }
                    .collectLatest { names ->
                        state = state.copy(availableAreaNames = names)
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
                state = state.copy(
                    searchterm = action.newsearchTerm
                )
            }


            is MainScreenAction.OnAddPoint -> {
                val cornerpoints = state.currentDrawingOffsets + action.offset

                val finished = cornerpoints.first() == cornerpoints.last() && cornerpoints.size >= 2
                state = state.copy(
                    currentDrawingOffsets = cornerpoints,
                    iteminfopopupshown = finished,//Finished drawing
                    isDrawing = !finished
                )
            }

            is MainScreenAction.OnSliderToggle -> {
                state = state.copy(
                    isDrawing = action.newvalue
                )
            }

            MainScreenAction.OnInfoDialogDismiss -> {
                state = state.copy(
                    iteminfopopupshown = false
                )
            }
            is MainScreenAction.OnInfoDialogSave -> {
                state = state.copy(
                    iteminfopopupshown = false,
                    currentDrawingOffsets = emptyList()
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
                        val area = areaRepository.getAreaByName(action.areaname)

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
        }
    }




}