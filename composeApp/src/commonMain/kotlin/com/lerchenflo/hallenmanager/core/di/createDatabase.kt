package com.lerchenflo.hallenmanager.core.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class CreateAppDatabase(private val builder: RoomDatabase.Builder<AppDatabase>) {

    fun getDatabase(): AppDatabase {
        return builder
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

}