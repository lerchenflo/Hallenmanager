package com.lerchenflo.hallenmanager.datasource.remote

import androidx.compose.ui.geometry.Offset
import com.lerchenflo.hallenmanager.datasource.AreaRepository
import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.mainscreen.domain.toArea
import com.lerchenflo.hallenmanager.mainscreen.domain.toAreaDto
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
import io.ktor.http.parameters
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.Json
import kotlin.time.Instant


class NetworkUtils(
    val httpClient: HttpClient,
) {
    private suspend fun networkRequest(
        serverURL: String,
        requestMethod: HttpMethod = HttpMethod.Get,
        requestParams: Map<String, String>? = null,
        body: Any? = null
    ): NetworkResult<String> {

        try {
            val response: HttpResponse = httpClient.request {

                //Set serverurl
                url(serverURL)

                //Set requesturl
                method = requestMethod

                //Timeout for the request
                timeout {
                    this.requestTimeoutMillis = 5_000L
                }

                //Append the request params
                parameters {
                    requestParams?.forEach { requestParam ->
                        append(requestParam.key, requestParam.value)
                    }
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




    data class AreaRequest(
        val areaid: String?,
        val name: String,
        val description: String,
    )

    suspend fun upsertArea(area: Area, remoteServer: NetworkConnection) : Area {
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
                    area
                }
                is NetworkResult.Success<*> -> {
                    val responseText = request.data.toString()
                    try {
                        val returned = Json.decodeFromString<Area>(responseText)

                        area.copy(
                            id = returned.id,
                            name = returned.name,
                            description = returned.description
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to parse area response: $responseText")
                        area
                    }
                }
            }
        }
        return area
    }


    data class IdTimeStamp(
        val id: String,
        val timeStamp: Instant
    )

    /*
    suspend fun areaSync(){

        areaRepository.getAllNetworkConnections().forEach { networkConnection ->

            //Get all timestamps for areas which are from this network connection
            val localtimestamps = areaRepository.getAllAreas()
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

            response
                .onSuccess { responseData ->
                    try {
                        val syncedAreas = Json.decodeFromString<List<Area>>(responseData)
                        syncedAreas.forEach { area ->
                            // Update or insert the areas in the database
                            areaRepository.upsertArea(area)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to parse sync response: $responseData")
                    }
                }
                .onError { errorMessage ->
                    println("Area sync network error for ${networkConnection.serverUrl}: $errorMessage")
                }



        }

    }

     */

    data class ItemRequest(
        val itemid: String,
        val areaId: String,
        val title: String,
        val description: String,
        val color: Long?,
        val layers: List<String>,
        val onArea: Boolean,
        var createdAt: Instant,
        var lastchangedAt: Instant,
        var lastchangedBy: String,
        val cornerPoints: List<Offset>
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
                layers = itemdto.layers,
                onArea = item.onArea,
                createdAt = item.createdAt,
                lastchangedAt = item.lastchangedAt,
                lastchangedBy = item.lastchangedBy,
                cornerPoints = item.cornerPoints
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
                    val returned = Json.decodeFromString<Item>(responseText)

                    area.copy(
                        id = returned.id,
                        name = returned.name,
                        description = returned.description
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Failed to parse area response: $responseText")
                    item
                }
            }
        }
    }


}