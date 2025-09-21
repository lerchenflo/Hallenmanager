package com.lerchenflo.hallenmanager.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.AreaWithItemsDto
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.ItemWithCornersDto
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Upsert
    suspend fun upsertAreaEntity(area: AreaDto): Long

    @Upsert
    suspend fun upsertItems(items: List<ItemDto>): List<Long>

    @Upsert
    suspend fun upsertItem(item: ItemDto): Long

    @Upsert
    suspend fun upsertCornerPoints(points: List<CornerPointDto>): List<Long>

    @Transaction
    suspend fun upsertItemWithCorners(item: ItemWithCornersDto) {

        val itemid = upsertItem(item.item)

        if (item.cornerPoints.isNotEmpty()) {
            val pointsWithIds = item.cornerPoints.map { cp ->
                cp.copy(itemId = itemid)
            }
            upsertCornerPoints(pointsWithIds)
        }
    }

    @Query("SELECT COUNT(*) FROM areas")
    suspend fun getAreaCount(): Int

    @Transaction
    suspend fun createDefaultArea(){
        upsertAreaEntity(AreaDto(
            id = 1L,
            name = "Area1",
            description = "Default area"
        ))
    }

    @Transaction
    @Query("SELECT * FROM itemdto WHERE areaId = :areaId")
    fun getItemsForAreaFlow(areaId: Long): Flow<List<ItemWithCornersDto>>


    @Transaction
    suspend fun upsertAreaWithItems(areaWithItems: AreaWithItemsDto) {
        // upsert area
        upsertAreaEntity(areaWithItems.area)

        // flatten items and their corner points
        val items = mutableListOf<ItemDto>()
        val cornerPoints = mutableListOf<CornerPointDto>()

        for (itemWithCorners in areaWithItems.items) {
            items += itemWithCorners.item
            cornerPoints += itemWithCorners.cornerPoints
        }

        if (items.isNotEmpty()) upsertItems(items)
        if (cornerPoints.isNotEmpty()) upsertCornerPoints(cornerPoints)
    }
}