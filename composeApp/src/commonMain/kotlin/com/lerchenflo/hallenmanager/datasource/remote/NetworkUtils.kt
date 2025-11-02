package com.lerchenflo.hallenmanager.datasource.remote

import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayer
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
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
import io.ktor.http.BadContentTypeFormatException
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.delay
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


    suspend fun upsertItem(item: Item, remoteServer: NetworkConnection) : Item {
        val requesturl = remoteServer.serverUrl + "/items"

        val itemdto = item.toItemDto(item.areaId)

        val request = networkRequest(
            serverURL = requesturl,
            requestMethod = HttpMethod.Post,
            requestParams = mapOf(
                "username" to remoteServer.userName
            ),
            body = ItemRequest(
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
        )

        return when (request) {
            is NetworkResult.Error<*> -> {
                println("Item upsert network error")
                item
            }
            is NetworkResult.Success<*> -> {
                val responseText = request.data.toString()
                try {
                    val response = Json.decodeFromString<ItemRequest>(responseText)

                    Item(
                        itemid = response.itemid,
                        areaId = response.areaId,
                        title = response.title,
                        description = response.description,
                        layers = database.areaDao().getLayersById(response.layers).mapNotNull {
                            it?.toLayer()
                        },
                        color = response.color,
                        onArea = response.onArea,
                        createdAt = response.createdAt,
                        lastchangedAt = response.lastchangedAt,
                        lastchangedBy = response.lastchangedBy,
                        cornerPoints = response.cornerPoints.map {
                            it.asOffset()
                        }
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Failed to parse area response: $responseText")
                    item
                }
            }
        }
    }

    suspend fun upsertLayer(layer: Layer) : Layer{
        //TODO network
        return layer
    }


}