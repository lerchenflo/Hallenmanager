package com.lerchenflo.hallenmanager.presentation

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

    val availableAreaNames : List<String> = emptyList<String>(),
    val currentArea: Area? = null,
    val availableLayers: List<Layer> = emptyList<Layer>()
) {
}


sealed interface MainScreenAction{
    data class OnSearchtermChange(val newsearchTerm: String) : MainScreenAction
    data class OnSliderToggle(val newvalue: Boolean) : MainScreenAction
    data class OnSelectArea(val areaname: String) : MainScreenAction
    data class OnAddPoint(val offset: Offset) : MainScreenAction
    data object OnInfoDialogDismiss : MainScreenAction
    data class OnInfoDialogSave(val item: Item) : MainScreenAction
    data class OnAreaDialogSave(val area: Area) : MainScreenAction
    data object OnCreateAreaStart: MainScreenAction
    data object OnAreaDialogDismiss : MainScreenAction

}
