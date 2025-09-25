package com.lerchenflo.hallenmanager.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    // Chat - Feature
    @Serializable
    data object HomeScreen: Route

    @Serializable
    data object Settings: Route
}