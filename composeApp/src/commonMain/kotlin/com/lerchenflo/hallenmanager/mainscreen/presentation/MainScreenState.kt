package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.mainscreen.presentation.search.SearchItem
import kotlin.collections.emptyList

data class MainScreenState(

    //Searching
    val searchterm: String = "", //Current searchterm in the searchbar
    val currentSearchResult: List<SearchItem> = emptyList(), //List of results for the search with the current searchterm

    //Current drawing
    val isDrawing: Boolean = false, //Is the user currently drawing a new item
    val currentDrawingOffsets: List<Offset> = emptyList(),  //List of points for the current drawing
    val gridspacing: Float = 400f,  //Current spacing of the canvas grid

    //Infopopup item
    val iteminfopopupshown : Boolean = false, //Is the info popup for an item shown
    val iteminfopopupItem: Item? = null, //Item which the popup is shown for

    //Infopopup area
    val areainfopopupshown : Boolean = false, //Is the area info popup for the current area shown
    val currentArea: Area? = null, //Area which is currently selected

    //Available items
    val availableAreas : List<AvailableArea> = emptyList(), //List of areas which are in the db
    val availableLayers: List<Layer> = emptyList<Layer>(), //List of all layers which are currently in the db
    val shortAccessItems: List<Item> = emptyList<Item>(),

    //Quick access menu
    val showShortAccessMenu : Boolean = false,  //Is the bottom short access menu shown

)




sealed interface MainScreenAction{

    data class OnSearchtermChange(val newsearchTerm: String) : MainScreenAction
    data class OnZoom(val scale: Float, val offset: Offset, val viewportsize: IntSize) : MainScreenAction
    data class OnClick(val contentpoint: Offset, val longpressed: Boolean) : MainScreenAction


    data object OnStartPainting : MainScreenAction
    data class OnAddPoint(val offset: Offset) : MainScreenAction
    data object OnStopPainting : MainScreenAction

    data class OnSelectArea(val areaid: Long) : MainScreenAction

    data object OnInfoDialogDismiss : MainScreenAction
    data class OnInfoDialogSave(val item: Item) : MainScreenAction
    data class OnAreaDialogSave(val area: Area) : MainScreenAction
    data object OnAreaDialogDismiss : MainScreenAction

    data object OnCreateAreaStart: MainScreenAction
    data class OnItemClicked(val item: Item) : MainScreenAction

    data class OnShowShortAccessMenuClick(val shown: Boolean) : MainScreenAction
    data object OnLayersClicked : MainScreenAction

    data class OnMoveItemToGrid(val item: Item, val position: Offset) : MainScreenAction
    data class OnMoveItemToShortAccess(val item: Item) : MainScreenAction


    data class CreateConnection(val serverurl: String, val userName: String, val alias: String): MainScreenAction
}


data class AvailableArea(
    val id: Long,
    val name: String,
    val description: String,
    val isSynced: Boolean
)

