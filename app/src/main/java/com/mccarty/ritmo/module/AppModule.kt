package com.mccarty.ritmo.module

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.codelab.android.datastore.AlbumPreference
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.data.AlbumPreferenceSerializer
import com.mccarty.ritmo.module.Constants.BASE_SPOTIFY_URL
import com.mccarty.ritmo.repository.remote.Constants.APPLICATION_JSON_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.AUTHORIZATION_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.CONTENT_TYPE_SPOTIFY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    private fun getOkHttp(): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(ApiClient.RequestInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_SPOTIFY_URL)
            .client(getOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providetDispatcher() = Dispatchers

    object RequestInterceptor : Interceptor {
        var token = ""
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
                .newBuilder()
                .addHeader(CONTENT_TYPE_SPOTIFY, APPLICATION_JSON_SPOTIFY)
                .addHeader(AUTHORIZATION_SPOTIFY, token)
                .build()
            return chain.proceed(request)
        }
    }

}