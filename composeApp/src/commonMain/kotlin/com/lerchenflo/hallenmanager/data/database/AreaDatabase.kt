package com.lerchenflo.hallenmanager.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.LayerDto
import com.lerchenflo.hallenmanager.data.relations.ItemLayerCrossRef

@Database(
    entities = [AreaDto::class, ItemDto::class, CornerPointDto::class, LayerDto::class, ItemLayerCrossRef::class],
    exportSchema = true,
    version = 7
)


@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun areaDao(): AreaDao


    companion object {
        const val DB_NAME = "database.db"
    }
}


expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>{
    override fun initialize(): AppDatabase
}