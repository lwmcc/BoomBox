package com.mccarty.ritmo.api

import android.content.Context
import com.mccarty.ritmo.module.Constants.BASE_SPOTIFY_URL
import com.mccarty.ritmo.repository.Constants
import com.mccarty.ritmo.repository.remote.Constants.APPLICATION_JSON_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.AUTHORIZATION_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.CONTENT_TYPE_SPOTIFY
import com.mccarty.ritmo.utils.hasNetworkConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Inject
import kotlin.jvm.Throws

class ApiClient {
    companion object RequestInterceptor : Interceptor {
        lateinit var context: Context
        var token = ""
        override fun intercept(chain: Interceptor.Chain): Response {

            //if (hasNetworkConnection(context)) {
                val request = chain.request()
                    .newBuilder()
                    .addHeader(CONTENT_TYPE_SPOTIFY, APPLICATION_JSON_SPOTIFY)
                    .addHeader(AUTHORIZATION_SPOTIFY, "Bearer $token")
                    .build()
                return chain.proceed(request)
          /*  } else {
                throw NoNetworkException()
            }*/
        }
    }

    class NoNetworkException internal constructor() :
        RuntimeException("There is no internet connection")
}