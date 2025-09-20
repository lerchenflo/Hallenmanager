package com.lerchenflo.hallenmanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lerchenflo.hallenmanager.core.App
import com.lerchenflo.hallenmanager.core.di.sharedmodule
import org.koin.core.context.startKoin

fun main() = application {

    startKoin {
        modules(sharedmodule)
    }



    Window(
        onCloseRequest = ::exitApplication,
        title = "Hallenmanager",
    ) {
        App()
    }
}