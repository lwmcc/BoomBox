package com.mccarty.ritmo.module

import com.mccarty.ritmo.domain.MediaDetails
import com.mccarty.ritmo.domain.MediaDetailsCollections
import com.mccarty.ritmo.domain.RemoteService
import com.mccarty.ritmo.domain.RemoteServiceControls
import com.mccarty.ritmo.domain.SliderTicker
import com.mccarty.ritmo.domain.Ticker
import com.mccarty.ritmo.domain.usecases.SetupMusicHeader
import com.mccarty.ritmo.domain.usecases.SetupMusicHeaderUseCase
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
    abstract fun bindSpotifyRemoteService(spotifyRemoteService: RemoteServiceControls): RemoteService

    @Binds
    abstract fun bindMediaDetailsCollections(mediaDetailsCollections: MediaDetailsCollections): MediaDetails

    @Binds
    abstract fun bindSliderTicker(sliderTicker: SliderTicker): Ticker

    @Binds
    abstract fun bindSetupMusicHeader(setupMusicHeaderUseCase: SetupMusicHeaderUseCase): SetupMusicHeader
}