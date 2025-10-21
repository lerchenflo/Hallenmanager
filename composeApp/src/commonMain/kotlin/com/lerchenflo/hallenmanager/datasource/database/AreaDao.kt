package com.lerchenflo.hallenmanager.datasource.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.lerchenflo.hallenmanager.datasource.remote.NetworkConnection
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.AreaWithItemsDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemLayerCrossRef
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
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
    suspend fun upsertConnection(connection: NetworkConnection)

    @Transaction
    @Upsert
    suspend fun upsertCornerPoints(points: List<CornerPointDto>): List<Long>

    @Upsert
    suspend fun upsertItemLayerCrossRef(crossRef: ItemLayerCrossRef)

    @Upsert
    suspend fun upsertItemLayerCrossRefs(crossRefs: List<ItemLayerCrossRef>)

    @Query("DELETE FROM itemlayercrossref WHERE itemId = :itemId")
    suspend fun deleteItemLayerCrossRefsForItem(itemId: Long)

    @Query("DELETE FROM cornerpointdto WHERE itemId = :itemId")
    suspend fun deleteCornerPointsForItem(itemId: Long)

    @Transaction
    suspend fun upsertItemWithCorners(item: ItemWithListsDto) {
        // Upsert the item first
        val returnedId = upsertItem(item.item)

        // Determine the actual item ID:
        // - If item.item.itemid is 0 (new item), use the returned ID
        // - Otherwise (existing item), use item.item.itemid
        val itemid = if (item.item.itemid == 0L) returnedId else item.item.itemid

        // Delete and re-insert corner points
        deleteCornerPointsForItem(itemid)
        if (item.cornerPoints.isNotEmpty()) {
            //println("ITEM upsert: Cornerpoints ${item.cornerPoints}")
            val pointsWithIds = item.cornerPoints.map { cp ->
                cp.copy(itemId = itemid)
            }
            upsertCornerPoints(pointsWithIds)
        }

        // Delete and re-insert layer cross-references
        deleteItemLayerCrossRefsForItem(itemid)
        if (item.layers.isNotEmpty()) {
            val crossRefs = item.layers.map { layer ->
                ItemLayerCrossRef(
                    itemid = itemid,
                    layerid = layer.layerid
                )
            }
            upsertItemLayerCrossRefs(crossRefs)
        }
    }

    @Query("SELECT COUNT(*) FROM areas")
    suspend fun getAreaCount(): Int

    @Transaction
    @Query("SELECT * FROM areas")
    fun getAreas(): Flow<List<AreaWithItemsDto>>

    @Transaction
    @Query("SELECT * FROM itemdto WHERE onArea = FALSE")
    fun getShortAccessItems() : Flow<List<ItemWithListsDto>>

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

    @Query("SELECT * FROM NetworkConnection")
    suspend fun getAllNetworkConnections(): List<NetworkConnection>


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
        layers.forEach {
            upsertLayer(it)
        }
    }

    @Transaction
    @Query("SELECT * FROM NetworkConnection WHERE id = :connectionid")
    suspend fun getNetworkConnectionById(connectionid: Long) : NetworkConnection
}