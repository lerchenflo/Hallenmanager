package com.lerchenflo.hallenmanager.core.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

class Navigator(
    val startDestination: Route,
) {
    private val _navigationActions = Channel<NavigationAction>()
    val navigationActions = _navigationActions.receiveAsFlow()

    suspend fun navigate(
        destination: Route,
        navOptions: NavOptionsBuilder.() -> Unit = {}
    ){
        _navigationActions.send(NavigationAction.Navigate(destination, navOptions))
    }

    suspend fun navigateUp(){
        _navigationActions.send(NavigationAction.NavigateUp)
    }
}