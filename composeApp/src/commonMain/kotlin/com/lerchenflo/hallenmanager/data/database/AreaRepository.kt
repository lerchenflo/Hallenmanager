package com.lerchenflo.hallenmanager.data.database

import com.lerchenflo.hallenmanager.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.domain.Area
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.Layer
import com.lerchenflo.hallenmanager.domain.toArea
import com.lerchenflo.hallenmanager.domain.toAreaDto
import com.lerchenflo.hallenmanager.domain.toItem
import com.lerchenflo.hallenmanager.domain.toItemDto
import com.lerchenflo.hallenmanager.domain.toLayer
import com.lerchenflo.hallenmanager.domain.toLayerDto
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

    suspend fun upsertLayer(layer: Layer) {
        database.areaDao().upsertLayer(layer.toLayerDto())
    }

    suspend fun upsertLayerList(layers: List<Layer>){
        database.areaDao().upsertLayerList(layers.map {
            it.toLayerDto()
        })
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

    fun getShortAccessItemsFlow(): Flow<List<Item>> {
        return database.areaDao().getShortAccessItems().map { items ->
            items.map {
                it.toItem()
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

    fun getAllItems(): Flow<List<Item>>{
        return database.areaDao().getAllItems().map { items ->
            items.map { itemdto ->
                itemdto.toItem()
            }
        }
    }

    fun getItemsFlow(areaid: Long): Flow<List<ItemWithListsDto>> {
        return database.areaDao().getItemsForAreaFlow(areaid)
    }

    fun getAllLayers(): Flow<List<Layer>>{
        return database.areaDao().getAllLayers().map { layerDtos ->
            layerDtos.map { layerDto ->
                layerDto.toLayer()
            }
                .sortedByDescending { it.sortId }
        }
    }

}