package com.mccarty.ritmo.module

import com.mccarty.ritmo.domain.SpotifyRemoteService
import com.mccarty.ritmo.domain.SpotifyService
import com.mccarty.ritmo.repository.remote.MusicRepository
import com.mccarty.ritmo.repository.remote.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class InterfaceModule {
    @Binds
    abstract fun bindRepository(musicRepository: MusicRepository): Repository

    @Binds
    abstract fun bindSpotifyRemoteService(spotifyRemoteService: SpotifyRemoteService): SpotifyService
}