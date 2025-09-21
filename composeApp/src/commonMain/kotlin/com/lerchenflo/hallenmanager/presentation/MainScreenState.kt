package com.lerchenflo.hallenmanager.presentation

import androidx.compose.ui.geometry.Offset
import com.lerchenflo.hallenmanager.data.Area
import com.lerchenflo.hallenmanager.data.Item
import kotlin.collections.emptyList

data class MainScreenState(
    val searchterm: String = "",
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val gridspacing: Float = 100f,
    val isDrawing: Boolean = false,
    val currentDrawingOffsets: List<Offset> = emptyList(),
    val infopopupshown : Boolean = false,
    val currentArea: Area = Area(
        name = "Area 1",
        description = "",
        items = emptyList()
    )
) {
}


sealed interface MainScreenAction{
    data class OnSearchtermChange(val newsearchTerm: String) : MainScreenAction
    data class OnZoom(val scale: Float, val offset: Offset) : MainScreenAction
    data class OnSliderToggle(val newvalue: Boolean) : MainScreenAction
    data class OnAddPoint(val offset: Offset) : MainScreenAction
    data object OnInfoDialogDismiss : MainScreenAction
    data class OnInfoDialogSave(val item: Item) : MainScreenAction
}
