package com.lerchenflo.hallenmanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lerchenflo.hallenmanager.core.App
import com.lerchenflo.hallenmanager.core.di.sharedmodule
import com.lerchenflo.hallenmanager.di.desktopAppDatabaseModule
import org.koin.core.context.startKoin

fun main() = application {

    startKoin {
        modules(sharedmodule, desktopAppDatabaseModule)
    }



    Window(
        onCloseRequest = ::exitApplication,
        title = "Hallenmanager",
    ) {
        App()
    }
}