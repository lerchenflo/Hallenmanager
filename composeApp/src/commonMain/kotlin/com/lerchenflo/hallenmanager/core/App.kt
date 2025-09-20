package com.lerchenflo.hallenmanager.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.hallenmanager.presentation.MainScreen
import com.lerchenflo.hallenmanager.presentation.MainScreenRoot
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
        }

    }
}