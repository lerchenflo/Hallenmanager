package com.lerchenflo.hallenmanager.datasource

import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import com.lerchenflo.hallenmanager.datasource.remote.NetworkConnection
import com.lerchenflo.hallenmanager.datasource.remote.NetworkUtils
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayerDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.mainscreen.domain.toArea
import com.lerchenflo.hallenmanager.mainscreen.domain.toAreaDto
import com.lerchenflo.hallenmanager.mainscreen.domain.toItem
import com.lerchenflo.hallenmanager.mainscreen.domain.toItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AreaRepository(
    private val database: AppDatabase,
    private val networkUtils: NetworkUtils
) {

    suspend fun upsertArea(area: Area) : Area {
        val newarea = networkUtils.upsertArea(area = area)
        return database.areaDao().upsertAreaWithItems(newarea!!.toAreaDto()).toArea()
    }

    suspend fun upsertItem(item: Item, areaid: Long) {
        database.areaDao().upsertItemWithCorners(item.toItemDto(areaid))
    }

    suspend fun upsertLayer(layer: Layer) {
        database.areaDao().upsertLayer(layer.toLayerDto())
    }

    suspend fun upsertConnection(connection: NetworkConnection) {
        database.areaDao().upsertConnection(connection)
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

    fun getAllItems(): Flow<List<Item>> {
        return database.areaDao().getAllItems().map { items ->
            items.map { itemdto ->
                itemdto.toItem()
            }
        }
    }

    fun getItemsFlow(areaid: Long): Flow<List<ItemWithListsDto>> {
        return database.areaDao().getItemsForAreaFlow(areaid)
    }

    fun getAllLayers(): Flow<List<Layer>> {
        return database.areaDao().getAllLayers().map { layerDtos ->
            layerDtos.map { layerDto ->
                layerDto.toLayer()
            }
                .sortedByDescending { it.sortId }
        }
    }

    suspend fun getAllNetworkConnections(): List<NetworkConnection> {
        return database.areaDao().getAllNetworkConnections()
    }

}