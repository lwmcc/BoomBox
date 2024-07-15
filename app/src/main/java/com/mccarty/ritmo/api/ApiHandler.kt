package com.mccarty.ritmo.api

import com.mccarty.networkrequest.network.NetworkRequest
import retrofit2.HttpException
import retrofit2.Response

interface ApiHandler {
    suspend fun <T : Any> handleApi(
        execute: suspend () -> Response<T>
    ): NetworkRequest<T> {
        return try {
            val response = execute()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkRequest.Success(body)
            } else {
                NetworkRequest.Error()
            }

        } catch (httpe: HttpException) { //  TODO: return error message
            NetworkRequest.Error()
        }
    }
}