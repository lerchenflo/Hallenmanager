package com.lerchenflo.hallenmanager.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.hallenmanager.core.navigation.NavigationAction
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.ObserveAsEvents
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.core.theme.AppTheme
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenRoot
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenViewmodel
import com.lerchenflo.hallenmanager.presentation.layerselection.LayerScreenRoot
import com.lerchenflo.hallenmanager.presentation.layerselection.LayerScreenViewmodel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()
        val navigator = koinInject<Navigator>()

        ObserveAsEvents(
            flow = navigator.navigationActions
        ){  action ->
            when(action){
                is NavigationAction.Navigate -> navController.navigate(action.destination){action.navOptions(this)}
                NavigationAction.NavigateUp -> navController.navigateUp()
            }
        }
        NavHost(
            navController = navController,
            startDestination = navigator.startDestination
        ) {
            composable<Route.HomeScreen> {
                val viewmodel = koinViewModel<MainScreenViewmodel>()
                MainScreenRoot(viewmodel)
            }

            composable<Route.Layers> {
                val viewmodel = koinViewModel<LayerScreenViewmodel>()
                LayerScreenRoot(viewmodel)
            }
        }

    }
}