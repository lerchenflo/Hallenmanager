package com.lerchenflo.hallenmanager.core.di

import com.lerchenflo.hallenmanager.data.database.AppDatabase
import com.lerchenflo.hallenmanager.data.database.AreaRepository
import com.lerchenflo.hallenmanager.presentation.MainScreenViewmodel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedmodule = module {

    viewModelOf(::MainScreenViewmodel)
    factory { MainScreenViewmodel(get()) } //Für desktop

    single <AppDatabase> { CreateAppDatabase(get()).getDatabase() }


    singleOf(::AreaRepository)

}