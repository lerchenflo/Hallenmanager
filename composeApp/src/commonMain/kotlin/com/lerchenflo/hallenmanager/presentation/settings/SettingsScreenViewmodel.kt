package com.lerchenflo.hallenmanager.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenState

class SettingsScreenViewmodel(
    private val areaRepository: AreaRepository,
    private val navigator: Navigator
): ViewModel() {

    var state by mutableStateOf(SettingsScreenState())
        private set

}