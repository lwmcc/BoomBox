package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.payload.PlaybackState
import com.mccarty.ritmo.model.payload.Playlist
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

interface Repository {
    suspend fun fetchRecentlyPlayedItem(): Flow<NetworkRequest<RecentlyPlayedItem>>
    suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>>
    suspend fun fetchCurrentlyPlayingTrack(): Flow<NetworkRequest<CurrentlyPlayingTrack>>
    suspend fun fetchPlayLists(): Flow<NetworkRequest<PlaylistData.PlaylistItem>>
    suspend fun fetchPlayList(playlistId: String): Flow<NetworkRequest<Playlist>>
    suspend fun fetchPlaybackState(): Flow<NetworkRequest<PlaybackState>>
}