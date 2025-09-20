package com.lerchenflo.hallenmanager.presentation

data class MainScreenState(
    val searchterm: String = ""
) {
}


sealed interface MainScreenAction{
    data class onsearchtermChange(val newsearchTerm: String) : MainScreenAction
}