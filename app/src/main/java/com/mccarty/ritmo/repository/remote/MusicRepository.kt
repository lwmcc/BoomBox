package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.domain.model.AlbumXX
import com.mccarty.ritmo.domain.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.domain.model.payload.PlaybackState
import com.mccarty.ritmo.domain.model.payload.Playlist
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import com.mccarty.ritmo.domain.model.payload.Seeds
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

    override suspend fun fetchUserPlayList(playlistId: String): Flow<NetworkRequest<Playlist>> = flow {
        emit(apiService.fetchUserPlayList(playlistId))
    }

    override suspend fun fetchPlaybackState(): Flow<NetworkRequest<PlaybackState>> = flow {
       emit(apiService.fetchPlaybackState())
    }

    override suspend fun fetchRecommendedPlaylists(trackIds: String, artistIds: String):
            Flow<NetworkRequest<Seeds>> = flow {
        emit(apiService.fetchRecommendedPlaylist(trackIds, artistIds))
    }
}
