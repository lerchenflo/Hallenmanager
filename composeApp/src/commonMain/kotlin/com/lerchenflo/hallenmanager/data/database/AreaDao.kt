package com.lerchenflo.hallenmanager.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.relations.AreaWithItemsDto
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.data.LayerDto
import com.lerchenflo.hallenmanager.domain.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Transaction
    @Upsert
    suspend fun upsertAreaEntity(area: AreaDto): Long

    @Transaction
    @Upsert
    suspend fun upsertItems(items: List<ItemDto>): List<Long>

    @Transaction
    @Upsert
    suspend fun upsertItem(item: ItemDto): Long

    @Transaction
    @Upsert
    suspend fun upsertLayer(layer: LayerDto): Long

    @Transaction
    @Upsert
    suspend fun upsertCornerPoints(points: List<CornerPointDto>): List<Long>

    @Transaction
    suspend fun upsertItemWithCorners(item: ItemWithListsDto) {

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
    @Query("SELECT * FROM areas")
    fun getAreas(): Flow<List<AreaWithItemsDto>>

    @Transaction
    @Query("SELECT * FROM areas WHERE id = :areaid")
    fun getAreaByIdFlow(areaid: Long) : Flow<AreaWithItemsDto?>

    @Transaction
    @Query("SELECT * FROM areas WHERE id = :areaid")
    suspend fun getAreaById(areaid: Long) : AreaWithItemsDto?

    @Transaction
    @Query("SELECT * FROM areas WHERE name = :areaname")
    suspend fun getAreaByName(areaname: String) : AreaWithItemsDto?

    @Transaction
    @Query("SELECT * FROM areas ORDER BY id ASC LIMIT 1")
    suspend fun getFirstArea(): AreaWithItemsDto?


    @Transaction
    @Query("SELECT * FROM itemdto WHERE areaId = :areaId")
    fun getItemsForAreaFlow(areaId: Long): Flow<List<ItemWithListsDto>>

    @Transaction
    @Query("SELECT * FROM itemdto")
    fun getAllItems(): Flow<List<ItemWithListsDto>>

    @Transaction
    @Query("SELECT * FROM LayerDto")
    fun getAllLayers(): Flow<List<LayerDto>>


    @Transaction
    suspend fun upsertAreaWithItems(areaWithItems: AreaWithItemsDto) : AreaWithItemsDto {
        // upsert area
        val areaid = upsertAreaEntity(areaWithItems.area)

        // flatten items and their corner points
        val items = mutableListOf<ItemDto>()
        val cornerPoints = mutableListOf<CornerPointDto>()

        for (itemWithCorners in areaWithItems.items) {
            items += itemWithCorners.item
            cornerPoints += itemWithCorners.cornerPoints
        }

        if (items.isNotEmpty()) upsertItems(items)
        if (cornerPoints.isNotEmpty()) upsertCornerPoints(cornerPoints)

        return getAreaById(areaid)!!
    }


    @Transaction
    suspend fun upsertLayerList(layers: List<LayerDto>) {
        for (i in layers.indices){
            upsertLayer(layers[i].copy(
                sortId = i
            ))
        }
    }
}