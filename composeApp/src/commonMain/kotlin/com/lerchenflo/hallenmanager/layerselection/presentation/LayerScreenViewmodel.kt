package com.lerchenflo.hallenmanager.layerselection.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.datasource.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LayerScreenViewmodel(
    private val appRepository: AppRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(LayerScreenState())
        private set


    init {
        viewModelScope.launch {
            appRepository.getAllLayersFlow().collectLatest {
                val layers = it.filter { layer ->
                    layer.networkConnectionId == state.selectedArea.networkConnectionId //Only show layers for the current area (If synced and where synced)
                }


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
                        appRepository.upsertLayer(action.layer)

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
                        appRepository.upsertLayerList(action.layers)
                    }
                }
            }

            is LayerScreenAction.OnLayerVisibilityChange -> {
                viewModelScope.launch {
                    appRepository.upsertLayer(action.layer.copy(
                        shown = action.visible
                    ))
                }
            }

            is LayerScreenAction.OnSelectArea -> {
                viewModelScope.launch {
                    val area = appRepository.getAreaById(action.areaid)

                    state = state.copy(
                        selectedArea = area!!
                    )
                }


            }
        }
    }
}