package com.lerchenflo.hallenmanager.layerselection.presentation

import com.lerchenflo.hallenmanager.layerselection.domain.Layer

data class LayerScreenState(
    val availableLayers: List<Layer> = emptyList<Layer>(),
    val addlayerpopupshown: Boolean = false,
    val selectedLayerPopupLayer: Layer? = null,
    val selectedAreaId: Long = 0L
)

sealed interface LayerScreenAction{
    data object OnNavigateBack : LayerScreenAction
    data object OnCreateLayerStart: LayerScreenAction
    data class OnCreateLayerSave(val layer: Layer): LayerScreenAction
    data object OnCreateLayerDismiss: LayerScreenAction
    data class OnLayerClick(val layer: Layer): LayerScreenAction
    data class OnLayerReorder(val layers: List<Layer>) : LayerScreenAction
    data class OnLayerVisibilityChange(val layer: Layer, val visible: Boolean) : LayerScreenAction

    data class OnSelectArea(val areaid: Long) : LayerScreenAction
}
