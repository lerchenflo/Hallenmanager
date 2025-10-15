package com.lerchenflo.hallenmanager.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import org.koin.dsl.module
import java.io.File

fun desktopAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    val appDataDir = when {
        os.contains("win") -> File(System.getenv("APPDATA"), "Schneaggchat")
        os.contains("mac") -> File(userHome, "Library/Application Support/Schneaggchat")
        else -> File(userHome, ".local/share/Schneaggchat")
    }

    if (!appDataDir.exists()){
        appDataDir.mkdirs()
    }
    val dbFile = File(appDataDir, AppDatabase.DB_NAME)
    return Room.databaseBuilder<AppDatabase>(dbFile.absolutePath)
}

val desktopAppDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { desktopAppDatabaseBuilder() }
}