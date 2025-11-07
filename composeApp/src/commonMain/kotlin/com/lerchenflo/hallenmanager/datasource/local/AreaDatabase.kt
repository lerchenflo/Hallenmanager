package com.lerchenflo.hallenmanager.datasource.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.lerchenflo.hallenmanager.datasource.remote.NetworkConnection
import com.lerchenflo.hallenmanager.layerselection.data.ItemLayerCrossRef
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto

@Database(
    entities = [
        AreaDto::class, //Saved area
        ItemDto::class, //Saved item
        CornerPointDto::class, //Saved cornerpoints for item
        LayerDto::class, //Saved layers
        ItemLayerCrossRef::class, //Cross reference between items and layers (m + n)
        NetworkConnection::class //Saved connections to servers + Username
               ],

    exportSchema = true,
    version = 12

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