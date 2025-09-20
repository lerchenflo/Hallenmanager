package com.lerchenflo.hallenmanager

import android.app.Application
import com.lerchenflo.hallenmanager.core.di.sharedmodule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainApp: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@MainApp)

            //Modules für room Userdatabase
            modules(sharedmodule)
        }



    }
}