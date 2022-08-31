package com.mccarty.ritmo.api

import com.mccarty.ritmo.module.AppModule
import com.mccarty.ritmo.module.Constants
import com.mccarty.ritmo.repository.remote.Constants.APPLICATION_JSON_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.AUTHORIZATION_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.CONTENT_TYPE_SPOTIFY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ApiClient(private val token: String) {
    private fun getOkHttp(token: String): OkHttpClient {
        RequestInterceptor.also {
            it.token = token
        }
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(RequestInterceptor)
            .build()
    }

    fun getRetrofit(token: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_SPOTIFY_URL)
            .client(getOkHttp(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService::class.java)
    }

    companion object RequestInterceptor : Interceptor {
        var token = ""
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
                .newBuilder()
                .addHeader(CONTENT_TYPE_SPOTIFY, APPLICATION_JSON_SPOTIFY)
                .addHeader(AUTHORIZATION_SPOTIFY, "Bearer $token")
                .build()
            return chain.proceed(request)
        }
    }
}

