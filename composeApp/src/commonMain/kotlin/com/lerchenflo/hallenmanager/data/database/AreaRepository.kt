package com.lerchenflo.hallenmanager.data.database

import com.lerchenflo.hallenmanager.data.ItemWithCornersDto
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.toAreaDto
import com.lerchenflo.hallenmanager.domain.toItemDto
import kotlinx.coroutines.flow.Flow

class AreaRepository(
    private val database: AppDatabase
) {

    suspend fun upsertArea(area: Area){
        database.areaDao().upsertAreaWithItems(area.toAreaDto())
    }

    suspend fun upsertItem(item: Item, areaid: Long) {
        database.areaDao().upsertItemWithCorners(item.toItemDto(areaid))
    }

    suspend fun getAreaCount(): Int {
        return database.areaDao().getAreaCount()
    }

    suspend fun createDefaultArea() {
        database.areaDao().createDefaultArea()
    }

    fun getItemsFlow(areaid: Long): Flow<List<ItemWithCornersDto>> {
        return database.areaDao().getItemsForAreaFlow(areaid)
    }

}