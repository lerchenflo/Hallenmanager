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
import com.lerchenflo.hallenmanager.mainscreen.domain.toAreaWithItemsDto
import com.lerchenflo.hallenmanager.mainscreen.domain.toItem
import com.lerchenflo.hallenmanager.mainscreen.domain.toItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Clock

class AreaRepository(
    private val database: AppDatabase,
    private val networkUtils: NetworkUtils
) {

    //TODO: Auto generate Keys and use different key if synced

    /**
     * Upsert an area to the local db and the remote server if synced
     */
    suspend fun upsertArea(area: Area) : Area {

        lateinit var returnedarea : Area

        if (area.isRemoteArea()) {
            val updatedArea = networkUtils.upsertArea(area = area, getNetworkConnectionById(area.networkConnectionId!!))

            returnedarea = upsertAreaToDB(updatedArea)
        } else {
            val fixedArea = area.copy(id = Clock.System.now().toEpochMilliseconds().toString())

            returnedarea = upsertAreaToDB(fixedArea)
        }

        return returnedarea
    }

    private suspend fun upsertAreaToDB(area: Area) : Area {
        val upsertedArea = database.areaDao().upsertAreaWithItems(areaWithItems = area.toAreaWithItemsDto())
        return upsertedArea.toArea()
    }


    suspend fun upsertItem(item: Item, areaid: String) : Item {

        lateinit var returneditem : Item

        val parentArea = database.areaDao().getAreaById(item.areaId)?.toArea()

        if (parentArea != null){
            if (parentArea.isRemoteArea()) {
                val updatedItem = networkUtils.upsertItem(item = item, getNetworkConnectionById(parentArea.networkConnectionId!!))

                returneditem = database.areaDao().upsertItemWithCorners(updatedItem.toItemDto(areaid)).toItem()
            } else {
                val fixedItem = item.copy(
                    itemid = Clock.System.now().toEpochMilliseconds().toString()
                )

                returneditem = database.areaDao().upsertItemWithCorners(fixedItem.toItemDto(areaid)).toItem()
            }
        }

        return returneditem

    }

    suspend fun upsertLayer(layer: Layer) : Layer {

        val updatedLayer = networkUtils.upsertLayer(layer)

        //TODO: IF online abfrage
        return database.areaDao().upsertLayer(updatedLayer)
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

    fun getAreas(): Flow<List<Area>> {
        return database.areaDao().getAreas().map { arealist ->
            arealist.map {areaWithItemsDto ->
                areaWithItemsDto.toArea()
            }
        }
    }

    suspend fun getAllAreas(): List<Area> {
        return database.areaDao().getAllAreas().map {
            it.toArea()
        }
    }


    fun getShortAccessItemsFlow(): Flow<List<Item>> {
        return database.areaDao().getShortAccessItems().map { items ->
            items.map {
                it.toItem()
            }
        }
    }

    fun getAreaByIdFlow(areaid: String = "") : Flow<Area> {
        return database.areaDao().getAreaByIdFlow(areaid).mapNotNull {
            it?.toArea()
        }
    }

    suspend fun getAreaById(areaId: String) : Area? {
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

    fun getItemsFlow(areaid: String): Flow<List<ItemWithListsDto>> {
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

    suspend fun getNetworkConnectionById(id: Long) : NetworkConnection{
        return database.areaDao().getNetworkConnectionById(id)
    }

    suspend fun syncNetworkElements(){
        val localConnections = database.areaDao().getAllNetworkConnections()
        val localAreas = database.areaDao().getAllAreas().map {
            it.toArea()
        }.filter { it.isRemoteArea() }

        println("LocalAreas: $localAreas localconnections: $localConnections")

        val newareas = networkUtils.areaSync(localConnections, localAreas)

        println("Network sync: $newareas")

        newareas.forEach { area ->
            database.areaDao().upsertAreaDto(area)
        }
    }
}