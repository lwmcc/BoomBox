package com.mccarty.ritmo.module

import android.content.Context
import com.mccarty.ritmo.repository.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DbModule {
    @Provides
    fun provideDataBase(@ApplicationContext context: Context) = AppDatabase.getDatabase(context)

    @Provides
    fun provideMusicDao(appDatabase: AppDatabase) = appDatabase.musicDao()
}