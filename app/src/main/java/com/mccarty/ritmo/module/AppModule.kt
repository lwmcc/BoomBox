package com.mccarty.ritmo.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mccarty.networkrequest.network.NetworkRequestAdapterFactory
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.api.MusicInterceptor
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

    private fun getOkHttp(
        connectivityManager: ConnectivityManager,
        sharedPreferences: SharedPreferences,
    ): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(
                MusicInterceptor(
                    sharedPreferences,
                    connectivityManager.getNetworkCapabilities(
                        connectivityManager.activeNetwork
                    )?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false,
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        connectivityManager: ConnectivityManager,
        sharedPreferences: SharedPreferences,
        ): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_SPOTIFY_URL)
            .client(getOkHttp(connectivityManager, sharedPreferences))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(NetworkRequestAdapterFactory.create())
            .build().create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDispatcher() = Dispatchers

    @Provides
    @Singleton
    fun provideConnectionManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun providePreferences(application: Application): SharedPreferences {
       return application.getSharedPreferences(MainActivity.SPOTIFY_TOKEN, Context.MODE_PRIVATE)
    }
}
