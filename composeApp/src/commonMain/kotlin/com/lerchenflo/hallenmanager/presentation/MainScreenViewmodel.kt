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
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainScreenViewmodel(
    private val areaRepository: AreaRepository
): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


    init {
        //Create default area if not exist
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                if (areaRepository.getAreaCount() == 0){
                    areaRepository.createDefaultArea()
                }
            }

            areaRepository.getItemsFlow(areaid = 1L).collect { items ->
                // Assuming MainScreenState has a property `items: List<ItemWithCornersDto>`
                state = state.copy(
                    currentArea = state.currentArea.copy(
                        items = items.map {
                            it.toItem()
                        }
                    )
                )
            }
        }



    }


    fun onAction(action: MainScreenAction) {
        when (action) {
            is MainScreenAction.OnSearchtermChange -> {
                state = state.copy(
                    searchterm = action.newsearchTerm
                )
            }

            is MainScreenAction.OnZoom -> {
                state = state.copy(
                    offset = action.offset,
                    scale = action.scale
                )
            }


            is MainScreenAction.OnAddPoint -> {
                val cornerpoints = state.currentDrawingOffsets + action.offset

                val finished = cornerpoints.first() == cornerpoints.last() && cornerpoints.size >= 2
                state = state.copy(
                    currentDrawingOffsets = cornerpoints,
                    infopopupshown = finished,//Finished drawing
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
                    infopopupshown = false
                )
            }
            is MainScreenAction.OnInfoDialogSave -> {
                state = state.copy(
                    infopopupshown = false,
                    currentDrawingOffsets = emptyList()
                )

                CoroutineScope(Dispatchers.IO).launch {
                    areaRepository.upsertItem(action.item, state.currentArea.id)
                }
            }
        }
    }




}