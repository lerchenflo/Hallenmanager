package com.lerchenflo.hallenmanager.layerselection.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.datasource.AreaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LayerScreenViewmodel(
    private val areaRepository: AreaRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(LayerScreenState())
        private set


    init {
        viewModelScope.launch {
            areaRepository.getAllLayersFlow().collectLatest { layers ->

                if (state.availableLayers != layers){
                    state = state.copy(
                        availableLayers = layers
                    )
                }



            }
        }
    }


    fun onAction(action: LayerScreenAction) {
        when (action) {
            LayerScreenAction.OnNavigateBack -> {
                viewModelScope.launch {
                    navigator.navigateUp()
                }
            }

            LayerScreenAction.OnCreateLayerStart -> {
                state = state.copy(
                    addlayerpopupshown = true
                )
            }

            LayerScreenAction.OnCreateLayerDismiss -> {
                state = state.copy(
                    addlayerpopupshown = false
                )
            }

            is LayerScreenAction.OnCreateLayerSave -> {
                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        areaRepository.upsertLayer(action.layer)

                        state = state.copy(
                            addlayerpopupshown = false,
                            selectedLayerPopupLayer = null
                        )
                    }
                }
            }

            is LayerScreenAction.OnLayerClick -> {
                state = state.copy(
                    addlayerpopupshown = true,
                    selectedLayerPopupLayer = action.layer
                )
            }

            is LayerScreenAction.OnLayerReorder -> {

                state = state.copy(
                    availableLayers = action.layers
                )

                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        areaRepository.upsertLayerList(action.layers)
                    }
                }
            }

            is LayerScreenAction.OnLayerVisibilityChange -> {
                viewModelScope.launch {
                    areaRepository.upsertLayer(action.layer.copy(
                        shown = action.visible
                    ))
                }
            }

            is LayerScreenAction.OnSelectArea -> {
                viewModelScope.launch {
                    val area = areaRepository.getAreaById(action.areaid)

                    state = state.copy(
                        selectedArea = area!!
                    )
                }


            }
        }
    }
}