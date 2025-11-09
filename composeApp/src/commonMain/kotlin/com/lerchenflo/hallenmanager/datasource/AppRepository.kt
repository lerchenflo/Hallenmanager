package com.lerchenflo.hallenmanager.datasource

import com.lerchenflo.hallenmanager.datasource.local.AppDatabase
import com.lerchenflo.hallenmanager.datasource.remote.NetworkConnection
import com.lerchenflo.hallenmanager.datasource.remote.NetworkUtils
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayer
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.mainscreen.domain.toArea
import com.lerchenflo.hallenmanager.mainscreen.domain.toAreaWithItemsDto
import com.lerchenflo.hallenmanager.mainscreen.domain.toItem
import com.lerchenflo.hallenmanager.mainscreen.domain.toItemDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty
import kotlin.time.Clock

class AppRepository(
    private val database: AppDatabase,
    private val networkUtils: NetworkUtils
) {





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
        val upsertedArea = database.dao().upsertAreaWithItems(areaWithItems = area.toAreaWithItemsDto())
        return upsertedArea.toArea()
    }




    suspend fun upsertItem(item: Item) : Item {

        lateinit var returneditem : Item

        val parentArea = database.dao().getAreaById(item.areaId)?.toArea()

        if (parentArea != null){
            if (parentArea.isRemoteArea()) {
                val remoteItem = networkUtils.upsertItem(item = item, getNetworkConnectionById(parentArea.networkConnectionId!!))

                return if (remoteItem != null){
                    database.dao().upsertItemWithCorners(remoteItem.toItemDto()).toItem()
                }else {
                    println("No network connection")
                    //TODO: SHow infopopup?
                    item
                }
            } else {
                val fixedItem = item.copy(
                    itemid = item.itemid.ifEmpty { Clock.System.now().toEpochMilliseconds().toString() }
                )

                returneditem = database.dao().upsertItemWithCorners(fixedItem.toItemDto()).toItem()
            }
        }

        return returneditem

    }

    suspend fun upsertLayer(layer: Layer) : Layer {

        lateinit var returnedlayer : Layer

        if (layer.isRemoteLayer()){
            val updatedLayer = networkUtils.upsertLayer(layer, getNetworkConnectionById(layer.networkConnectionId!!))

            return if (updatedLayer != null){
                database.dao().upsertLayer(layer = updatedLayer)
            }else {
                println("No network connection")
                layer
            }
        } else {
            val fixedLayer = layer.copy(
                layerid = layer.layerid.ifEmpty { Clock.System.now().toEpochMilliseconds().toString() }
            )

            returnedlayer = database.dao().upsertLayer(fixedLayer)
        }

        return returnedlayer
    }

    suspend fun upsertConnection(connection: NetworkConnection) {
        database.dao().upsertConnection(connection)
    }

    suspend fun upsertLayerList(layers: List<Layer>){
        layers.forEach { layer ->
            upsertLayer(layer)
        }
    }





    suspend fun getAreaCount(): Int {
        return database.dao().getAreaCount()
    }

    fun getAreas(): Flow<List<Area>> {
        return database.dao().getAreas().map { arealist ->
            arealist.map {areaWithItemsDto ->
                areaWithItemsDto.toArea()
            }
        }
    }

    suspend fun getAllAreas(): List<Area> {
        return database.dao().getAllAreas().map {
            it.toArea()
        }
    }


    fun getShortAccessItemsFlow(): Flow<List<Item>> {
        return database.dao().getShortAccessItems().map { items ->
            items.map {
                it.toItem()
            }
        }
    }

    fun getAreaByIdFlow(areaid: String = "") : Flow<Area> {
        return database.dao().getAreaByIdFlow(areaid).mapNotNull {
            it?.toArea()
        }
    }

    suspend fun getAreaById(areaId: String) : Area? {
        return database.dao().getAreaById(areaId)?.toArea()
    }

    suspend fun getFirstArea() : Area? {
        return database.dao().getFirstArea()?.toArea()
    }

    fun getAllItems(): Flow<List<Item>> {
        return database.dao().getAllItemsFlow().map { items ->
            items.map { itemdto ->
                itemdto.toItem()
            }
        }
    }

    fun getItemsFlow(areaid: String): Flow<List<ItemWithListsDto>> {
        return database.dao().getItemsForAreaFlow(areaid)
    }

    fun getAllLayersFlow(): Flow<List<Layer>> {
        return database.dao().getAllLayersFlow().map { layerDtos ->
            layerDtos.map { layerDto ->
                layerDto.toLayer()
            }
                .sortedByDescending { it.sortId }
        }
    }




    suspend fun getAllNetworkConnections(): List<NetworkConnection> {
        return database.dao().getAllNetworkConnections()
    }

    fun getAllNetworkConnectionsFlow(): Flow<List<NetworkConnection>> {
        return database.dao().getAllNetworkConnectionsFlow()
    }

    suspend fun getNetworkConnectionById(id: Long) : NetworkConnection{
        return database.dao().getNetworkConnectionById(id)
    }


    private val _timestampCache = MutableStateFlow<String>("0")
    val timestampCache: StateFlow<String> = _timestampCache.asStateFlow()


    fun observeTimestamps(scope: CoroutineScope) {
        scope.launch {
            database.dao().getLatestAreaTimestamp()
                .collect { timestamp ->
                    timestamp?.let {
                        updateTimeStamp(timestamp)
                    }
                }
        }

        // Observe item timestamps
        scope.launch {
            database.dao().getLatestItemTimestamp()
                .collect { timestamp ->
                    timestamp?.let {
                        updateTimeStamp(timestamp)

                    }
                }
        }

        // Observe layer timestamps
        scope.launch {
            database.dao().getLatestLayerTimestamp()
                .collect { timestamp ->
                    timestamp?.let {
                        updateTimeStamp(timestamp)
                    }
                }
        }
    }

    private fun updateTimeStamp(timestamp: String) {
        if (_timestampCache.value.toLong() < timestamp.toLong()){
            _timestampCache.value = timestamp
        }
    }



    suspend fun syncNetworkElements(ignoreLocal : Boolean = false){

        val localConnections = database.dao().getAllNetworkConnections()


        val latestservertimestamp = networkUtils.getLatestChangeTime(localConnections)

        if (!ignoreLocal){
            if (latestservertimestamp == null || latestservertimestamp.toLong() <= timestampCache.value.toLong()){
                println("No remote changes")
                return
            }
        }



        println("Sync gets executed")
        val localAreas = database.dao().getAllAreas().map {
            it.toArea()
        }.filter { it.isRemoteArea() }

        val localItems = database.dao().getAllItems().map {
            it.toItem()
        }.filter { it.isRemoteItem() }

        val localLayers = database.dao().getAllLayers()
            .map { it.toLayer() }
            .filter { it.isRemoteLayer() }

        val arearesponse = networkUtils.areaSync(localConnections, localAreas)
        arearesponse.first.forEach { area ->
            database.dao().upsertAreaDto(area)
        }
        //Delete areas
        arearesponse.second.forEach { areaid ->
            database.dao().deleteAreaDtoById(areaid)
        }




        val itemandcornersync = networkUtils.itemSync(localConnections, localItems)
        itemandcornersync.first.first.forEach { itemDto ->
            database.dao().upsertItem(itemDto)
        }
        database.dao().upsertCornerPoints(itemandcornersync.first.second)
        itemandcornersync.first.third.forEach { crossRef ->
            database.dao().insertItemLayerCrossRef(crossRef)
        }

        //Delete items
        itemandcornersync.second.forEach { itemid ->
            database.dao().deleteItemById(itemid)
        }


        val newlayers = networkUtils.layerSync(localConnections, localLayers)
        newlayers.first.forEach { layerDto ->
            database.dao().upsertLayer(layerdto = layerDto)
        }

        newlayers.second.forEach { layerid ->
            database.dao().deleteLayerById(layerid)
        }
        println("Local item count: ${localItems.count()}")
        println("Local Area count: ${localAreas.count()}")
        println("Local Layer count: ${localLayers.count()}")

        println("Upserted new items from sync: ${itemandcornersync.first.first.count()}")
        println("Upserted new areas from sync: ${arearesponse.first.count()}")
        println("Upserted new layers from sync: ${newlayers.first.count()}")

        println("DELETING AREAS: ${arearesponse.second.count()}")
        println("DELETING LAYERS: ${newlayers.second.count()}")
        println("DELETING ITEMS: ${itemandcornersync.second.count()}")
    }
}