package com.lerchenflo.hallenmanager.presentation

import androidx.compose.ui.geometry.Offset

data class MainScreenState(
    val searchterm: String = "",
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isDrawing: Boolean = false
) {
}


sealed interface MainScreenAction{
    data class OnSearchtermChange(val newsearchTerm: String) : MainScreenAction
    data class OnZoom(val scale: Float, val offset: Offset) : MainScreenAction
    data class OnStartDrawing(val offset: Offset) : MainScreenAction
}