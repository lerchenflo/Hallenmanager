package com.lerchenflo.hallenmanager.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt

class MainScreenViewmodel(

): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


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

                //TODO: Item ind datenbank
            }
        }
    }




}