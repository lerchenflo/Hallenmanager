package com.lerchenflo.hallenmanager.datasource.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.lerchenflo.hallenmanager.datasource.remote.NetworkConnection
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayer
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.AreaWithItemsDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Transaction
    suspend fun upsertAreaDto(area: AreaDto): AreaDto {
        val existing = getAreaById(area.id)
        if (existing != null) {
            updateAreaDto(area)
        } else {
            insertAreaDto(area)
        }
        val returnedarea = getAreaDtoById(area.id)!!
        return returnedarea
    }

    @Update
    suspend fun updateAreaDto(area: AreaDto)

    @Insert
    suspend fun insertAreaDto(area: AreaDto)


    @Query("SELECT COUNT(*) FROM areas")
    suspend fun getAreaCount(): Int

    @Transaction
    @Query("SELECT * FROM areas")
    fun getAreas(): Flow<List<AreaWithItemsDto>>

    @Transaction
    @Query("SELECT * FROM areas")
    suspend fun getAllAreas(): List<AreaWithItemsDto>

    @Transaction
    @Query("SELECT * FROM areas WHERE id = :areaid")
    fun getAreaByIdFlow(areaid: String) : Flow<AreaWithItemsDto?>

    @Transaction
    @Query("SELECT * FROM areas WHERE id = :areaid")
    suspend fun getAreaById(areaid: String) : AreaWithItemsDto?

    @Query("SELECT * FROM areas WHERE id = :areaid")
    suspend fun getAreaDtoById(areaid: String) : AreaDto?

    @Transaction
    @Query("SELECT * FROM areas WHERE name = :areaname")
    suspend fun getAreaByName(areaname: String) : AreaWithItemsDto?

    @Transaction
    @Query("SELECT * FROM areas ORDER BY id ASC LIMIT 1")
    suspend fun getFirstArea(): AreaWithItemsDto?






    @Transaction
    suspend fun upsertItem(item: ItemDto): ItemDto {
        val existing = getItemById(item.itemid)
        if (existing != null) {
            updateItem(item)
        } else {
            insertItem(item)
        }
        return getItemById(item.itemid)!!
    }

    @Query("SELECT * FROM itemdto WHERE itemid = :itemId")
    suspend fun getItemById(itemId: String): ItemDto?
    @Update
    suspend fun updateItem(item: ItemDto)
    @Insert
    suspend fun insertItem(item: ItemDto)





    @Transaction
    @Query("SELECT * FROM itemdto WHERE onArea = FALSE")
    fun getShortAccessItems() : Flow<List<ItemWithListsDto>>

    @Transaction
    @Query("SELECT * FROM itemdto WHERE areaId = :areaId")
    fun getItemsForAreaFlow(areaId: String): Flow<List<ItemWithListsDto>>

    @Transaction
    @Query("SELECT * FROM itemdto")
    fun getAllItemsFlow(): Flow<List<ItemWithListsDto>>

    @Transaction
    @Query("SELECT * FROM itemdto")
    suspend fun getAllItems(): List<ItemWithListsDto>

    @Transaction
    @Query("SELECT * FROM itemdto WHERE itemid = :itemId")
    suspend fun getItemWithListsById(itemId: String): ItemWithListsDto?





    @Transaction
    suspend fun upsertLayer(layer: LayerDto): LayerDto {
        val existing = getLayerById(layer.layerid)
        if (existing != null) {
            updateLayerDto(layer)
        } else {
            insertLayerDto(layer)
        }
        return getLayerDtoById(layer.layerid)!!
    }

    @Update
    suspend fun updateLayerDto(layer: LayerDto)

    @Insert
    suspend fun insertLayerDto(layer: LayerDto)

    @Query("SELECT * FROM LayerDto WHERE layerid = :id LIMIT 1")
    suspend fun getLayerById(id: String): LayerDto?

    suspend fun getLayersById(ids: List<String>): List<LayerDto?> {
        return ids.map { layerid ->
            getLayerById(layerid)
        }
    }

    @Query("SELECT * FROM LayerDto WHERE layerid = :id LIMIT 1")
    suspend fun getLayerDtoById(id: String): LayerDto?

    @Transaction
    @Query("SELECT * FROM LayerDto")
    fun getAllLayers(): Flow<List<LayerDto>>

    @Transaction
    suspend fun upsertLayerList(layers: List<LayerDto>) {
        layers.forEach {
            upsertLayer(it)
        }
    }







    @Transaction
    @Upsert
    suspend fun upsertConnection(connection: NetworkConnection)

    @Query("SELECT * FROM NetworkConnection")
    suspend fun getAllNetworkConnections(): List<NetworkConnection>

    @Query("SELECT * FROM NetworkConnection")
    fun getAllNetworkConnectionsFlow(): Flow<List<NetworkConnection>>

    @Transaction
    @Query("SELECT * FROM NetworkConnection WHERE id = :connectionid")
    suspend fun getNetworkConnectionById(connectionid: Long) : NetworkConnection





    @Transaction
    suspend fun upsertCornerPoint(cornerPointDto: CornerPointDto): CornerPointDto {
        val existing = getCornerPointById(cornerPointDto.id)
        if (existing != null) {
            updateCornerPoint(cornerPointDto)
        } else {
            insertCornerPoint(cornerPointDto)
        }
        return getCornerPointById(cornerPointDto.id)!!
    }

    @Query("SELECT * FROM cornerpointdto WHERE id = :id")
    suspend fun getCornerPointById(id: String): CornerPointDto?
    @Update
    suspend fun updateCornerPoint(cornerPointDto: CornerPointDto)
    @Insert
    suspend fun insertCornerPoint(cornerPointDto: CornerPointDto)

    @Transaction
    @Upsert
    suspend fun upsertCornerPoints(points: List<CornerPointDto>)

    @Query("DELETE FROM cornerpointdto WHERE itemId = :itemId")
    suspend fun deleteCornerPointsForItem(itemId: String)









    @Transaction
    suspend fun upsertAreaWithItems(areaWithItems: AreaWithItemsDto) : AreaWithItemsDto {
        // upsert area
        val area = upsertAreaDto(areaWithItems.area)

        // flatten items and their corner points
        val items = mutableListOf<ItemDto>()
        val cornerPoints = mutableListOf<CornerPointDto>()

        for (itemWithCorners in areaWithItems.items) {
            items += itemWithCorners.item
            cornerPoints += itemWithCorners.cornerPoints
        }

        items.forEach { itemDto ->
            upsertItem(itemDto)
        }
        if (cornerPoints.isNotEmpty()) upsertCornerPoints(cornerPoints)

        return getAreaById(area.id)!!
    }

    @Transaction
    suspend fun upsertItemWithCorners(item: ItemWithListsDto) : ItemWithListsDto {
        // Upsert the item first
        val updatedItem = upsertItem(item.item)


        val itemid = if (item.item.itemid == "") updatedItem.itemid else item.item.itemid

        // Delete and re-insert corner points
        deleteCornerPointsForItem(itemid)
        if (item.cornerPoints.isNotEmpty()) {
            //println("ITEM upsert: Cornerpoints ${item.cornerPoints}")
            val pointsWithIds = item.cornerPoints.map { cp ->
                cp.copy(itemId = itemid)
            }
            upsertCornerPoints(pointsWithIds)
        }

        return getItemWithListsById(updatedItem.itemid)!!
    }


    @Transaction
    suspend fun upsertLayer(layer: Layer) : Layer {
        val upsertedlayer = upsertLayer(layer = layer)

        return getLayerById(upsertedlayer.layerid)!!.toLayer()
    }


}