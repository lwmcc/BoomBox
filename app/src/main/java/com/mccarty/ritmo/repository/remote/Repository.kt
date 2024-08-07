package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.domain.model.AlbumXX
import com.mccarty.ritmo.domain.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.domain.model.payload.PlaybackState as PlaybackState
import com.mccarty.ritmo.domain.model.payload.Playlist
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.domain.model.payload.Seeds
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun fetchRecentlyPlayedItem(): Flow<NetworkRequest<RecentlyPlayedItem>>
    suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>>
    suspend fun fetchCurrentlyPlayingTrack(): Flow<NetworkRequest<CurrentlyPlayingTrack>>
    suspend fun fetchPlayLists(): Flow<NetworkRequest<PlaylistData.PlaylistItem>>
    suspend fun fetchUserPlayList(playlistId: String): Flow<NetworkRequest<Playlist>>
    suspend fun fetchPlaybackState(): Flow<NetworkRequest<PlaybackState>>
    suspend fun fetchRecommendedPlaylists(trackIds: String, artistIds: String): Flow<NetworkRequest<Seeds>>
}