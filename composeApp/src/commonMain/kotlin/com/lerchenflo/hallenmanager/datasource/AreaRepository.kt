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
import kotlin.text.ifEmpty
import kotlin.time.Clock

class AreaRepository(
    private val database: AppDatabase,
    private val networkUtils: NetworkUtils
) {

    //TODO: Auto generate Keys and use different key if synced

    /**
     * Upsert an area to the local db and the remote server if synced
     */
    suspend fun upsertArea(area: Area): Area {


        if (area.isRemoteArea()) {
            val remoteareaId = networkUtils.upsertArea(area = area, getNetworkConnectionById(area.networkConnectionId!!))
            return if (remoteareaId != null){
                upsertAreaToDB(area.copy(
                    id = remoteareaId
                ))
            }else {
                throw Exception("No network connection")
            }
        } else {
            val fixedArea = area.copy(id = Clock.System.now().toEpochMilliseconds().toString())
            return upsertAreaToDB(fixedArea)
        }
    }

    private suspend fun upsertAreaToDB(area: Area) : Area {
        val upsertedArea = database.areaDao().upsertAreaWithItems(areaWithItems = area.toAreaWithItemsDto())
        return upsertedArea.toArea()
    }




    suspend fun upsertItem(item: Item) : Item {

        lateinit var returneditem : Item

        val parentArea = database.areaDao().getAreaById(item.areaId)?.toArea()

        if (parentArea != null){
            if (parentArea.isRemoteArea()) {
                val remoteItem = networkUtils.upsertItem(item = item, getNetworkConnectionById(parentArea.networkConnectionId!!))

                return if (remoteItem != null){
                    database.areaDao().upsertItemWithCorners(remoteItem.toItemDto()).toItem()
                }else {
                    println("No network connection")
                    //TODO: SHow infopopup?
                    item
                }
            } else {
                val fixedItem = item.copy(
                    itemid = item.itemid.ifEmpty { Clock.System.now().toEpochMilliseconds().toString() }
                )

                returneditem = database.areaDao().upsertItemWithCorners(fixedItem.toItemDto()).toItem()
            }
        }

        return returneditem

    }

    suspend fun upsertLayer(layer: Layer) : Layer {

        lateinit var returnedlayer : Layer

        if (layer.isRemoteLayer()){
            val updatedLayer = networkUtils.upsertLayer(layer, getNetworkConnectionById(layer.networkConnectionId!!))

            return if (updatedLayer != null){
                database.areaDao().upsertLayer(layer = updatedLayer)
            }else {
                println("No network connection")
                layer
            }
        } else {
            val fixedLayer = layer.copy(
                layerid = layer.layerid.ifEmpty { Clock.System.now().toEpochMilliseconds().toString() }
            )

            returnedlayer = database.areaDao().upsertLayer(fixedLayer)
        }

        return returnedlayer
    }

    suspend fun upsertConnection(connection: NetworkConnection) {
        database.areaDao().upsertConnection(connection)
    }

    suspend fun upsertLayerList(layers: List<Layer>){
        layers.forEach { layer ->
            upsertLayer(layer)
        }
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
        return database.areaDao().getAllItemsFlow().map { items ->
            items.map { itemdto ->
                itemdto.toItem()
            }
        }
    }

    fun getItemsFlow(areaid: String): Flow<List<ItemWithListsDto>> {
        return database.areaDao().getItemsForAreaFlow(areaid)
    }

    fun getAllLayersFlow(): Flow<List<Layer>> {
        return database.areaDao().getAllLayersFlow().map { layerDtos ->
            layerDtos.map { layerDto ->
                layerDto.toLayer()
            }
                .sortedByDescending { it.sortId }
        }
    }




    suspend fun getAllNetworkConnections(): List<NetworkConnection> {
        return database.areaDao().getAllNetworkConnections()
    }

    fun getAllNetworkConnectionsFlow(): Flow<List<NetworkConnection>> {
        return database.areaDao().getAllNetworkConnectionsFlow()
    }

    suspend fun getNetworkConnectionById(id: Long) : NetworkConnection{
        return database.areaDao().getNetworkConnectionById(id)
    }

    suspend fun syncNetworkElements(){
        val localConnections = database.areaDao().getAllNetworkConnections()

        val localAreas = database.areaDao().getAllAreas().map {
            it.toArea()
        }.filter { it.isRemoteArea() }

        val localItems = database.areaDao().getAllItems().map {
            it.toItem()
        }.filter { it.isRemoteItem() }

        val localLayers = database.areaDao().getAllLayers()
            .map { it.toLayer() }
            .filter { it.isRemoteLayer() }

        val newareas = networkUtils.areaSync(localConnections, localAreas)
        newareas.forEach { area ->
            database.areaDao().upsertAreaDto(area)
        }



        println("Local item count: ${localItems.count()}")
        val itemandcornersync = networkUtils.itemSync(localConnections, localItems)
        println("Upserted new items from sync: ${itemandcornersync.first.count()}")
        println("Upserted new Cornerpoints from sync: ${itemandcornersync.second.count()}")
        println("Upserted new LayerCrossRefs from sync: ${itemandcornersync.third.count()}")

        itemandcornersync.first.forEach { itemDto ->
            database.areaDao().upsertItem(itemDto)
        }
        database.areaDao().upsertCornerPoints(itemandcornersync.second)
        itemandcornersync.third.forEach { crossRef ->
            database.areaDao().insertItemLayerCrossRef(crossRef)
        }

        val newlayers = networkUtils.layerSync(localConnections, localLayers)
        newlayers.forEach { layerDto ->
            database.areaDao().upsertLayer(layerdto = layerDto)
        }

    }
}