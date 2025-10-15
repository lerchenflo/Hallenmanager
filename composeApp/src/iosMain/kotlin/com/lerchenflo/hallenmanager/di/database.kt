package com.lerchenflo.hallenmanager.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

val IosDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { iosAppDatabaseBuilder() }
}


@OptIn(ExperimentalForeignApi::class)
fun iosAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {

    val documentDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )



    val dbFile = documentDir?.path + "/" + AppDatabase.DB_NAME

    return Room.databaseBuilder<AppDatabase>(
        name = dbFile
    )
}