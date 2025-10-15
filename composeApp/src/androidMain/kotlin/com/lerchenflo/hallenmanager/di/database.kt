package com.lerchenflo.hallenmanager.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { androidAppDatabaseBuilder(androidContext()) }
}

fun androidAppDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase>{
    val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}