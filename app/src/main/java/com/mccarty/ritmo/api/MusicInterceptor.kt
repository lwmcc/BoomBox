package com.mccarty.ritmo.api

import android.content.SharedPreferences
import android.net.NetworkCapabilities
import com.mccarty.ritmo.MainActivity.Companion.SPOTIFY_TOKEN
import com.mccarty.ritmo.repository.remote.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class MusicInterceptor @Inject constructor(
    private val networkCapabilities: NetworkCapabilities,
    private val sharedPreferences: SharedPreferences,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            val request = chain.request()
                .newBuilder()
                .addHeader(Constants.CONTENT_TYPE_SPOTIFY, Constants.APPLICATION_JSON_SPOTIFY)
                .addHeader(Constants.AUTHORIZATION_SPOTIFY, "Bearer ${readPreferences(sharedPreferences)}")
                .build()
            return chain.proceed(request)
        } else {
            throw NoNetworkException()
        }
    }

    private fun readPreferences(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(SPOTIFY_TOKEN, "")
    }

    class NoNetworkException internal constructor() :
        RuntimeException("There is no internet connection")
}