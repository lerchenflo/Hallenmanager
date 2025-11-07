package com.lerchenflo.hallenmanager.core.di

import com.lerchenflo.hallenmanager.core.navigation.Navigator
import com.lerchenflo.hallenmanager.core.navigation.Route
import com.lerchenflo.hallenmanager.datasource.local.AppDatabase
import com.lerchenflo.hallenmanager.datasource.AreaRepository
import com.lerchenflo.hallenmanager.datasource.remote.NetworkUtils
import com.lerchenflo.hallenmanager.mainscreen.presentation.MainScreenViewmodel
import com.lerchenflo.hallenmanager.layerselection.presentation.LayerScreenViewmodel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedmodule = module {

    viewModelOf(::MainScreenViewmodel)
    factory { MainScreenViewmodel(get(), get()) } //Für desktop

    viewModelOf(::LayerScreenViewmodel)
    factory { LayerScreenViewmodel(get(), get()) }

    single <AppDatabase> { CreateAppDatabase(get()).getDatabase() }
    single <HttpClient> { createHttpClient(get()) }


    singleOf(::NetworkUtils)
    singleOf(::AreaRepository)

    single<Navigator> {
        Navigator(Route.HomeScreen)
    }

}