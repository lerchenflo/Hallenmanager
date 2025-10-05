package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.collection.IntSet
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.Layer
import com.lerchenflo.hallenmanager.presentation.homescreen.search.SearchItem
import kotlin.collections.emptyList

data class MainScreenState(
    val searchterm: String = "",
    val gridspacing: Float = 400f,
    val isDrawing: Boolean = false,
    val currentSearchResult: List<SearchItem> = emptyList(),
    val currentDrawingOffsets: List<Offset> = emptyList(),
    val iteminfopopupshown : Boolean = false,
    val iteminfopopupItem: Item? = null,
    val areainfopopupshown : Boolean = false,
    val availableAreas : List<AvailableArea> = emptyList(),
    val currentArea: Area? = Area(name = "testarea", description = "testetestest", items = emptyList()),
    val availableLayers: List<Layer> = emptyList<Layer>(),
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
    data class OnZoom(val scale: Float, val offset: Offset, val viewportsize: IntSize) : MainScreenAction
    data class OnItemClicked(val item: Item) : MainScreenAction

    data object OnLayersClicked : MainScreenAction

}



data class AvailableArea(
    val id: Long,
    val name: String,
    val description: String
)
