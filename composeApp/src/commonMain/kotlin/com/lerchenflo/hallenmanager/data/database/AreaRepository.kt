package com.lerchenflo.hallenmanager.data.database

import com.lerchenflo.hallenmanager.data.ItemWithCornersDto
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.toArea
import com.lerchenflo.hallenmanager.domain.toAreaDto
import com.lerchenflo.hallenmanager.domain.toItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AreaRepository(
    private val database: AppDatabase
) {

    suspend fun upsertArea(area: Area) : Area{
        return  database.areaDao().upsertAreaWithItems(area.toAreaDto()).toArea()
    }

    suspend fun upsertItem(item: Item, areaid: Long) {
        database.areaDao().upsertItemWithCorners(item.toItemDto(areaid))
    }

    suspend fun getAreaCount(): Int {
        return database.areaDao().getAreaCount()
    }

    suspend fun getAreas(): Flow<List<Area>> {
        return database.areaDao().getAreas().map {
            it.map {
                it.toArea()
            }
        }
    }

    fun getAreaByIdFlow(areaid: Long = 0L) : Flow<Area?> {
        return database.areaDao().getAreaByIdFlow(areaid).map {
            it?.toArea()
        }
    }

    suspend fun getAreaById(areaId: Long) : Area? {
        return database.areaDao().getAreaById(areaId)?.toArea()
    }

    suspend fun getFirstArea() : Area? {
        return database.areaDao().getFirstArea()?.toArea()
    }


    suspend fun createDefaultArea() {
        database.areaDao().createDefaultArea()
    }

    fun getItemsFlow(areaid: Long): Flow<List<ItemWithCornersDto>> {
        return database.areaDao().getItemsForAreaFlow(areaid)
    }

}