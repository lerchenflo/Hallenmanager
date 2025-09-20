package com.lerchenflo.hallenmanager.core

import kotlinx.serialization.Serializable

sealed interface Route {

    // Chat - Feature
    @Serializable
    data object HomeScreen: Route
}