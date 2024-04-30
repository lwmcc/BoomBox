package com.mccarty.ritmo.module

import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.repository.remote.RepositoryInt
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class InterfaceModule {
    @Binds
    abstract fun bindRepository(imple: Repository): RepositoryInt
}