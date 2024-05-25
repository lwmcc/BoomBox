package com.mccarty.ritmo.module

import android.content.Context
import com.mccarty.networkrequest.network.NetworkRequestAdapterFactory
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.module.Constants.BASE_SPOTIFY_URL
import com.mccarty.ritmo.repository.db.AppDatabase
import com.mccarty.ritmo.repository.local.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context.applicationContext

    @Provides
    @Singleton
    fun provideLocalRepository(context: Context, db: AppDatabase): LocalRepository {
        return LocalRepository(context, db)
    }

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
            .baseUrl(BASE_SPOTIFY_URL)
            .client(getOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkRequestAdapterFactory.create())
            .build().create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providetDispatcher() = Dispatchers

//    object RequestInterceptor : Interceptor {
//        var token = ""
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request()
//                .newBuilder()
//                .addHeader(CONTENT_TYPE_SPOTIFY, APPLICATION_JSON_SPOTIFY)
//                .addHeader(AUTHORIZATION_SPOTIFY, token)
//                .build()
//            return chain.proceed(request)
//        }
//    }

}