package com.lerchenflo.hallenmanager.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainScreenViewmodel(

): ViewModel() {

    var state by mutableStateOf(MainScreenState())
        private set


    fun onAction(action: MainScreenAction) {
        when (action) {
            is MainScreenAction.onsearchtermChange -> {
                state = state.copy(
                    searchterm = action.newsearchTerm
                )
            }
        }
    }
}