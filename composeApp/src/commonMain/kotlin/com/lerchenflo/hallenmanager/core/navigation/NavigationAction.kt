package com.lerchenflo.hallenmanager.core.navigation

import androidx.navigation.NavOptionsBuilder

sealed interface NavigationAction{
    data class Navigate(
        val destination: Route,
        val navOptions: NavOptionsBuilder.() -> Unit = {}
    ) : NavigationAction

    data object NavigateUp: NavigationAction

}