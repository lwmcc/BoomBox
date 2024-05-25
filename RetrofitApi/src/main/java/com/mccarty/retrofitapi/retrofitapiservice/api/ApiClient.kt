package com.mccarty.retrofitapiservice.api

import android.content.Context
import com.mccarty.retrofitapi.retrofitapiservice.utils.hasNetworkConnection
import okhttp3.Interceptor
import okhttp3.Response

class ApiClient {
    companion object RequestInterceptor : Interceptor {
        lateinit var context: Context
        var token = ""
        const val AUTHORIZATION_SPOTIFY = "Authorization"
        const val CONTENT_TYPE_SPOTIFY = "Content-Type"
        const val APPLICATION_JSON_SPOTIFY = "application/json"
        override fun intercept(chain: Interceptor.Chain): Response {
            if (hasNetworkConnection(context)) {
                val request = chain.request()
                    .newBuilder()
                    .addHeader(CONTENT_TYPE_SPOTIFY, APPLICATION_JSON_SPOTIFY)
                    .addHeader(AUTHORIZATION_SPOTIFY, "Bearer $token")
                    .build()
                return chain.proceed(request)
            } else {
                throw NoNetworkException()
            }
        }
    }

    class NoNetworkException internal constructor() :
        RuntimeException("There is no internet connection")
}