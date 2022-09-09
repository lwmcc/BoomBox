package com.mccarty.ritmo.module

import android.app.Application
import android.content.Context
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.module.Constants.BASE_SPOTIFY_URL
import com.mccarty.ritmo.repository.db.AppDatabase
import com.mccarty.ritmo.repository.local.LocalRepository
import com.mccarty.ritmo.repository.remote.Constants.APPLICATION_JSON_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.AUTHORIZATION_SPOTIFY
import com.mccarty.ritmo.repository.remote.Constants.CONTENT_TYPE_SPOTIFY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
internal object DbModule {
    @Provides
    fun provideDataBase(@ApplicationContext context: Context) = AppDatabase.getDatabase(context)

    @Provides
    fun provideMusicDao(appDatabase: AppDatabase) = appDatabase.musicDao()
}