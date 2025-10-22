package com.lerchenflo.hallenmanager.datasource.remote

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<E>(val error: E) : NetworkResult<E>()
}

suspend fun <T> NetworkResult<T>.onSuccess(block: suspend (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        block(this.data)
    }
    return this
}

suspend fun <E> NetworkResult<E>.onError(block: suspend (E) -> Unit): NetworkResult<E> {
    if (this is NetworkResult.Error) {
        block(this.error)
    }
    return this
}