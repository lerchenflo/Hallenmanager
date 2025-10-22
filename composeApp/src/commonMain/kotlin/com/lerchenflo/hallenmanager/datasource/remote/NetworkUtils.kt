package com.lerchenflo.hallenmanager.datasource.remote

import com.lerchenflo.hallenmanager.datasource.database.AppDatabase
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import com.lerchenflo.hallenmanager.mainscreen.domain.toArea
import com.lerchenflo.hallenmanager.mainscreen.domain.toAreaDto
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
    val database: AppDatabase
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

    suspend fun upsertArea(area: Area) : Area? {
        if (area.isRemoteArea()){
            val remoteserver = database.areaDao().getNetworkConnectionById(area.networkConnectionId!!)

            val requesturl = remoteserver.serverUrl + "/areas"

            val request = networkRequest(
                serverURL = requesturl,
                requestMethod = HttpMethod.Post,
                requestParams = mapOf(
                    "username" to remoteserver.userName
                ),
                body = AreaRequest(
                    areaid = area.serverId,
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
                        val returned = Json.decodeFromString<Area>(responseText)

                        area.copy(
                            serverId = returned.serverId ?: area.serverId,
                            name = returned.name,
                            description = returned.description
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Failed to parse area response: $responseText")
                        null
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

    suspend fun areaSync(){

        database.areaDao().getAllNetworkConnections().forEach { networkConnection ->

            //Get all timestamps for areas which are from this network connection
            val localtimestamps = database.areaDao().getAllAreas()
                .mapNotNull { entity ->
                    val area = entity.toArea()
                    if (area.isRemoteArea() && area.serverId != null && area.networkConnectionId == networkConnection.id) {
                        IdTimeStamp(
                            id = area.serverId!!,
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
                            database.areaDao().upsertAreaEntity(area.toAreaDto().area)
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



}