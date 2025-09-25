package com.lerchenflo.hallenmanager.core.di

import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.data.database.AppDatabase
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.presentation.homescreen.MainScreenViewmodel
import com.lerchenflo.hallenmanager.presentation.settings.SettingsScreenViewmodel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedmodule = module {

    viewModelOf(::MainScreenViewmodel)
    factory { MainScreenViewmodel(get(), get()) } //Für desktop

    viewModelOf(::SettingsScreenViewmodel)
    factory { SettingsScreenViewmodel(get()) }

    single <AppDatabase> { CreateAppDatabase(get()).getDatabase() }



    singleOf(::AreaRepository)

    single<Navigator> {
        Navigator(Route.HomeScreen)
    }

}