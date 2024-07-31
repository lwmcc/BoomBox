package com.mccarty.networkrequest.network

sealed class NetworkRequest<T: Any> {
    data class Error<T: Any>(val message: String): NetworkRequest<T>()

    data class Success<T: Any>(val data: T): NetworkRequest<T>()
}