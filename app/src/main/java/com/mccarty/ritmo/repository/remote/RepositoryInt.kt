package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.RecentlyPlayedTrack
import com.mccarty.ritmo.model.TrackV2Item
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface RepositoryInt {
    suspend fun fetchRecentlyPlayedItem(): Flow<NetworkRequest<RecentlyPlayedItem>>
    suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>>
    suspend fun fetchCurrentlyPlayingTrack(): Flow<NetworkRequest<CurrentlyPlayingTrack>> // CurrentlyPlayingTrack
    suspend fun fetchPlayList(): Flow<NetworkRequest<PlaylistData.PlaylistItem>>
}