package com.lerchenflo.hallenmanager.datasource.remote

import com.lerchenflo.hallenmanager.mainscreen.domain.Area
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


sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<E>(val error: E) : NetworkResult<E>()
}

class NetworkUtils(
    val httpClient: HttpClient
) {
    suspend inline fun <reified T> networkRequest(
        serverURL: String,
        requestMethod: HttpMethod = HttpMethod.Get,
        requestParams: Map<String, String>? = null,
        body: T? = null
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


    suspend fun upsertArea(area: Area) {

    }


}