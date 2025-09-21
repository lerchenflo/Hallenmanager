package com.lerchenflo.hallenmanager

import androidx.compose.ui.window.ComposeUIViewController
import com.lerchenflo.hallenmanager.core.App
import com.lerchenflo.hallenmanager.core.di.sharedmodule
import com.lerchenflo.hallenmanager.di.IosDatabaseModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(sharedmodule, IosDatabaseModule)
        }
    }
) { App() }