package com.lerchenflo.hallenmanager.presentation.settings

data class SettingsScreenState(
    val oda: Boolean = false
)

sealed interface SettingsScreenAction{
    data object OnNavigateBack : SettingsScreenAction
}
