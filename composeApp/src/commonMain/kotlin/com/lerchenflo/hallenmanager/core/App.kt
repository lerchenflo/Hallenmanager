package com.lerchenflo.hallenmanager.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenRoot
import com.lerchenflo.hallenmanager.presentation.settings.SettingsScreenRoot
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Route.HomeScreen
        ) {
            composable<Route.HomeScreen> {
                MainScreenRoot()
            }

            composable<Route.Settings> {
                SettingsScreenRoot()
            }
        }

    }
}