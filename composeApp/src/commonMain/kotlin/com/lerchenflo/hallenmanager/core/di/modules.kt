package com.lerchenflo.hallenmanager.core.di

import com.lerchenflo.hallenmanager.presentation.MainScreenViewmodel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedmodule = module {

    viewModelOf(::MainScreenViewmodel)
    factory { MainScreenViewmodel() } //Für desktop

}