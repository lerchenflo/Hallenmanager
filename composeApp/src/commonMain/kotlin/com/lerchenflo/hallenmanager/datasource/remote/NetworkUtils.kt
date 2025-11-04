package com.lerchenflo.hallenmanager.datasource.remote

import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.mainscreen.domain.toItemDto
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class NetworkUtils(
    val httpClient: HttpClient,
    val database: AppDatabase
) {
    private suspend fun networkRequest(
        serverURL: String,
        requestMethod: HttpMethod = HttpMethod.Get,
        requestParams: Map<String, String>? = null,
        body: Any? = null
    ): NetworkResult<String> {

        try {
            val urlWithParams = if (requestParams != null && requestParams.isNotEmpty()) {
                val queryString = requestParams.entries.joinToString("&") { (key, value) ->
                    "$key=$value"
                }
                "$serverURL?$queryString"
            } else {
                serverURL
            }


            val response: HttpResponse = httpClient.request {

                //Set serverurl
                url(urlWithParams)

                //Set requesturl
                method = requestMethod

                //Timeout for the request
                timeout {
                    this.requestTimeoutMillis = 5_000L
                }


                if (body != null &&
                    (requestMethod == HttpMethod.Post ||
                            requestMethod == HttpMethod.Put ||
                            requestMethod == HttpMethod.Patch)) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }

            }

            return NetworkResult.Success(response.bodyAsText())

        } catch (e: UnresolvedAddressException) {
            return NetworkResult.Error("Serveraddress not found")
        } catch (e: HttpRequestTimeoutException) {
            return NetworkResult.Error("Request timed out")
        } catch (e: SocketTimeoutException) {
            return NetworkResult.Error("Request timed out")
        } catch (e: Exception) {
            e.printStackTrace()
            return NetworkResult.Error("Unknown error ")
        }
    }


    /**
     * Test if a backend is running on the selected server url
     * @param baseserverURL The base server url, for example http://192.168.0.100:8080
     */
    suspend fun testServer(baseserverURL: String): Boolean {

        var requesturl = baseserverURL.removeSuffix("/")
        requesturl += "/test"

        val request = networkRequest(
            serverURL = requesturl,
            requestMethod = HttpMethod.Get,
        )

        return when (request) {
            is NetworkResult.Error<*> -> {
                false
            }

            is NetworkResult.Success<*> -> {
                request.data.toString() == "server found"
            }
        }
    }



    @Serializable
    data class AreaRequest(
        val areaid: String,
        val name: String,
        val description: String,
    )

    @Serializable
    data class AreaResponse(
        val id: String,
        val name: String,
        val description: String,
        val createdAt: String,
        var lastchangedAt: String,
        var lastchangedBy: String,
    )

    /**
     * Upserts an area to the selected networkconnection and returns the areaid
     */
    suspend fun upsertArea(area: Area, remoteServer: NetworkConnection) : String? {
        if (area.isRemoteArea()){

            val requesturl = remoteServer.serverUrl + "/areas"

            val request = networkRequest(
                serverURL = requesturl,
                requestMethod = HttpMethod.Post,
                requestParams = mapOf(
                    "username" to remoteServer.userName
                ),
                body = AreaRequest(
                    areaid = area.id,
                    name = area.name,
                    description = area.description
                )
            )

            return when (request) {
                is NetworkResult.Error<*> -> {
                    println("Area upsert network error")
                    null
                }
                is NetworkResult.Success<*> -> {
                    val responseText = request.data.toString()
                    try {
                        val returned = Json.decodeFromString<AreaResponse>(responseText)

                        returned.id
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to parse area response: $responseText")
                        null
                    }
                }
            }
        }
        return null
    }

    @Serializable
    data class IdTimeStamp(
        val id: String,
        val timeStamp: String
    )


    suspend fun areaSync(networkConnections: List<NetworkConnection>, localAreas: List<Area>): List<AreaDto> {
        val allNewAreas = mutableListOf<AreaDto>()

        networkConnections.forEach { networkConnection ->
            // Get all timestamps for areas which are from this network connection
            val localtimestamps = localAreas
                .mapNotNull { area ->
                    if (area.isRemoteArea() && area.networkConnectionId == networkConnection.id) {
                        IdTimeStamp(
                            id = area.id,
                            timeStamp = area.lastchangedAt
                        )
                    } else null
                }

            val response = networkRequest(
                serverURL = networkConnection.serverUrl + "/areas/sync",
                requestMethod = HttpMethod.Post,
                requestParams = mapOf("username" to networkConnection.userName),
                body = localtimestamps
            )

            when (response) {
                is NetworkResult.Error<*> -> {
                    println("sync error")
                }
                is NetworkResult.Success<*> -> {
                    try {
                        val newareas = Json.decodeFromString<List<AreaResponse>>(response.data.toString())

                        newareas.forEach { areaResponse ->
                            allNewAreas.add(AreaDto(
                                id = areaResponse.id,
                                name = areaResponse.name,
                                description = areaResponse.description,
                                createdAt = areaResponse.createdAt,
                                lastchangedAt = areaResponse.lastchangedAt,
                                lastchangedBy = areaResponse.lastchangedBy,
                                networkConnectionId = networkConnection.id
                            ))
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to parse sync response: ${response.data.toString()}")
                    }
                }
            }
        }

        return allNewAreas
    }


    @Serializable
    data class ItemRequest(
        val itemid: String,
        val areaId: String,
        val title: String,
        val description: String,
        val color: Long?,
        val layers: List<String>,
        val onArea: Boolean,
        var createdAt: String,
        var lastchangedAt: String,
        var lastchangedBy: String,
        val cornerPoints: List<CornerPointDto>
    )

    @Serializable
    data class ItemResponse(
        val itemid: String,
        var createdAt: String,
        var lastchangedAt: String,
        var lastchangedBy: String,
        val cornerPoints: List<CornerPointResponse>,
    )

    @Serializable
    data class CornerPointResponse(
        val id: String = "",
        val itemId: String,
        var offsetX: Float,
        var offsetY: Float
    )


    suspend fun upsertItem(item: Item, remoteServer: NetworkConnection) : Item? {
        val requesturl = remoteServer.serverUrl + "/items"

        val itemdto = item.toItemDto()

        val requestitem = ItemRequest(
            itemid = item.itemid,
            areaId = item.areaId,
            title = item.title,
            description = item.description,
            color = item.color,
            layers = itemdto.layers.map {
                it.layerid
            },
            onArea = item.onArea,
            createdAt = item.createdAt,
            lastchangedAt = item.lastchangedAt,
            lastchangedBy = item.lastchangedBy,
            cornerPoints = itemdto.cornerPoints
        )

        println("Requestitem: $requestitem")

        val request = networkRequest(
            serverURL = requesturl,
            requestMethod = HttpMethod.Post,
            requestParams = mapOf(
                "username" to remoteServer.userName
            ),
            body = requestitem
        )

        return when (request) {
            is NetworkResult.Error<*> -> {
                println("Item upsert network error")
                null
            }
            is NetworkResult.Success<*> -> {
                val responseText = request.data.toString()
                try {
                    val parsedResponse = Json.decodeFromString<ItemResponse>(responseText)


                    item.copy(
                        itemid = parsedResponse.itemid,
                        createdAt = parsedResponse.createdAt,
                        lastchangedAt = parsedResponse.lastchangedBy,
                        lastchangedBy = parsedResponse.lastchangedBy,
                        cornerPoints = parsedResponse.cornerPoints.map {
                            CornerPointDto(
                                id = it.id,
                                itemId = it.itemId,
                                offsetX = it.offsetX,
                                offsetY = it.offsetY,
                                networkConnectionId = item.networkConnectionId
                            )
                        }
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Failed to parse area response: $responseText")
                    null
                }
            }
        }
    }



    suspend fun itemSync(
        networkConnections: List<NetworkConnection>,
        localItems: List<Item>
    ): Pair<List<ItemDto>, List<CornerPointDto>> = supervisorScope {
        val deferredResults = networkConnections.map { networkConnection ->
            async {
                val itemsForThisConnection = mutableListOf<ItemDto>()
                val cornerPointsForThisConnection = mutableListOf<CornerPointDto>()

                // Build timestamps for this connection
                val localtimestamps = localItems
                    .asSequence()
                    .filter { it.networkConnectionId == networkConnection.id }
                    .map { IdTimeStamp(id = it.itemid, timeStamp = it.lastchangedAt) }
                    .toList()

                try {
                    // perform network request on IO dispatcher (if networkRequest is blocking)
                    val response = withContext(Dispatchers.IO) {
                        networkRequest(
                            serverURL = networkConnection.serverUrl + "/items/sync",
                            requestMethod = HttpMethod.Post,
                            requestParams = mapOf("username" to networkConnection.userName),
                            body = localtimestamps
                        )
                    }

                    when (response) {
                        is NetworkResult.Error<*> -> {
                            println("sync error for ${networkConnection.id}: ${response.error}")
                        }
                        is NetworkResult.Success<*> -> {
                            try {
                                val newitems = Json.decodeFromString<List<ItemRequest>>(response.data.toString())

                                newitems.forEach { itemRequest ->
                                    itemsForThisConnection.add(
                                        ItemDto(
                                            itemid = itemRequest.itemid,
                                            title = itemRequest.title,
                                            areaId = itemRequest.areaId,
                                            description = itemRequest.description,
                                            createdAt = itemRequest.createdAt,
                                            lastchangedAt = itemRequest.lastchangedAt,
                                            lastchangedBy = itemRequest.lastchangedBy,
                                            color = itemRequest.color,
                                            onArea = itemRequest.onArea,
                                            networkConnectionId = networkConnection.id
                                        )
                                    )

                                    itemRequest.cornerPoints.forEach { point ->
                                        cornerPointsForThisConnection.add(
                                            CornerPointDto(
                                                id = point.id,
                                                itemId = point.itemId,
                                                offsetX = point.offsetX,
                                                offsetY = point.offsetY,
                                                networkConnectionId = networkConnection.id
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                println("Failed to parse sync response for ${networkConnection.id}: ${response.data}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // catch any unexpected exception for this connection so it doesn't kill others
                    e.printStackTrace()
                    println("Unexpected error syncing ${networkConnection.id}: ${e.message}")
                }

                Pair(itemsForThisConnection, cornerPointsForThisConnection)
            }.await()
        }

        val allNewItems = deferredResults.flatMap { it.first }
        val allNewCornerpoints = deferredResults.flatMap { it.second }

        Pair(allNewItems, allNewCornerpoints)
    }




    suspend fun upsertLayer(layer: Layer) : Layer{
        //TODO network
        return layer
    }


}