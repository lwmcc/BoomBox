package com.mccarty.retrofitapiservice.dagger.module

import com.mccarty.networkrequest.network.NetworkRequestAdapterFactory
import com.mccarty.retrofitapi.retrofitapiservice.api.ApiService
import com.mccarty.retrofitapiservice.api.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private fun getOkHttp(): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(ApiClient.RequestInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com")
            .client(getOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkRequestAdapterFactory.create())
            .build().create(ApiService::class.java)
    }
}
