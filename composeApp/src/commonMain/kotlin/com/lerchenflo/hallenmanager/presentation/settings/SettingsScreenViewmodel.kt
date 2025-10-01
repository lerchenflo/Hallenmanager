package com.lerchenflo.hallenmanager.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenAction
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsScreenViewmodel(
    private val areaRepository: AreaRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(SettingsScreenState())
        private set


    init {
        viewModelScope.launch {
            areaRepository.getAllLayers().collectLatest { layers ->
                state = state.copy(
                    availableLayers = layers
                )
            }
        }
    }


    fun onAction(action: SettingsScreenAction) {
        when (action) {
            SettingsScreenAction.OnNavigateBack -> {
                viewModelScope.launch {
                    navigator.navigateUp()
                }
            }

            SettingsScreenAction.OnCreateLayerStart -> {
                state = state.copy(
                    addlayerpopupshown = true
                )
            }

            SettingsScreenAction.OnCreateLayerDismiss -> {
                state = state.copy(
                    addlayerpopupshown = false
                )
            }

            is SettingsScreenAction.OnCreateLayerSave -> {
                viewModelScope.launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        areaRepository.upsertLayer(action.layer)
                    }
                }
            }

        }
    }
}