package com.lerchenflo.hallenmanager.datasource.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemLayerCrossRef

@Database(
    entities = [AreaDto::class, ItemDto::class, CornerPointDto::class, LayerDto::class, ItemLayerCrossRef::class],
    exportSchema = true,
    version = 2,

)


@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun areaDao(): AreaDao


    companion object {
        const val DB_NAME = "database.db"
    }


}


expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>{
    override fun initialize(): AppDatabase
}