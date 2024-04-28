package com.mccarty.networkrequest.network

sealed class NetworkRequest<T: Any> {
    class Error<T: Any>: NetworkRequest<T>()
    data class Success<T: Any>(val data: T): NetworkRequest<T>()
}