package com.lerchenflo.hallenmanager.presentation.layerselection

import com.lerchenflo.hallenmanager.domain.Layer

data class LayerScreenState(
    val availableLayers: List<Layer> = emptyList<Layer>(),
    val addlayerpopupshown: Boolean = false,
    val selectedLayerPopupLayer: Layer? = null,
)

sealed interface LayerScreenAction{
    data object OnNavigateBack : LayerScreenAction
    data object OnCreateLayerStart: LayerScreenAction
    data class OnCreateLayerSave(val layer: Layer): LayerScreenAction
    data object OnCreateLayerDismiss: LayerScreenAction
    data class OnLayerClick(val layer: Layer): LayerScreenAction
    data class OnLayerReorder(val layers: List<Layer>) : LayerScreenAction
    data class OnLayerVisibilityChange(val layer: Layer, val visible: Boolean) : LayerScreenAction
}
