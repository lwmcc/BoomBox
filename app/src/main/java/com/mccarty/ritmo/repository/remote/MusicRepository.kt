package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class MusicRepository @Inject constructor(private val apiService: ApiService): Repository {

    override suspend fun fetchRecentlyPlayedItem(): Flow<NetworkRequest<RecentlyPlayedItem>>  = flow {
        emit(apiService.fetchRecentlyPlayedItem())
    }

    override suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>> = flow {
        emit(apiService.fetchAlbumInfo(id))
    }

    override suspend fun fetchCurrentlyPlayingTrack(): Flow<NetworkRequest<CurrentlyPlayingTrack>> = flow {
        emit(apiService.fetchCurrentlyPlayingTrack())
    }

    override suspend fun fetchPlayLists(): Flow<NetworkRequest<PlaylistData.PlaylistItem>> = flow {
        emit(apiService.fetchPlayLists())
    }

    override suspend fun fetchPlayList(playlistId: String): Flow<NetworkRequest<PlaylistData.PlaylistItem>> = flow {
        emit(apiService.fetchPlayList(playlistId))
    }
}
