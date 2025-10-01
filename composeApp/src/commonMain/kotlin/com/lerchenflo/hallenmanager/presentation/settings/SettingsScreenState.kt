package com.lerchenflo.hallenmanager.presentation.settings

import com.lerchenflo.hallenmanager.domain.Layer

data class SettingsScreenState(
    val availableLayers: List<Layer> = emptyList<Layer>(),
    val addlayerpopupshown: Boolean = false,
    val selectedLayerPopupLayer: Layer? = null,
)

sealed interface SettingsScreenAction{
    data object OnNavigateBack : SettingsScreenAction
    data object OnCreateLayerStart: SettingsScreenAction
    data class OnCreateLayerSave(val layer: Layer): SettingsScreenAction
    data object OnCreateLayerDismiss: SettingsScreenAction

}
