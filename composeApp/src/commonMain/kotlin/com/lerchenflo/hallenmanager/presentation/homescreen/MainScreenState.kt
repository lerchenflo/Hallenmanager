package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.ui.geometry.Offset
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.Layer
import kotlin.collections.emptyList

data class MainScreenState(
    val searchterm: String = "",
    val gridspacing: Float = 100f,
    val isDrawing: Boolean = false,
    val currentDrawingOffsets: List<Offset> = emptyList(),
    val iteminfopopupshown : Boolean = false,
    val areainfopopupshown : Boolean = false,
    val availableAreas : List<AvailableArea> = emptyList(),
    val currentArea: Area? = null,
    val availableLayers: List<Layer> = emptyList<Layer>()
)




sealed interface MainScreenAction{
    data class OnSearchtermChange(val newsearchTerm: String) : MainScreenAction
    data object OnStopPainting : MainScreenAction
    data class OnSelectArea(val areaid: Long) : MainScreenAction
    data class OnAddPoint(val offset: Offset) : MainScreenAction
    data object OnInfoDialogDismiss : MainScreenAction
    data class OnInfoDialogSave(val item: Item) : MainScreenAction
    data class OnAreaDialogSave(val area: Area) : MainScreenAction
    data object OnCreateAreaStart: MainScreenAction
    data object OnAreaDialogDismiss : MainScreenAction

}



data class AvailableArea(
    val id: Long,
    val name: String,
    val description: String
)
