package com.mccarty.ritmo.repository.remote

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.RecentlyPlayedTrack
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface RepositoryInt {
    suspend fun fetchRecentlyPlayedTracks(): Flow<NetworkRequest<List<RecentlyPlayedTrack>>>
    suspend fun fetchRecentlyPlayedMusic(): Flow<NetworkRequest<RecentlyPlayedItem>>

    suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>>
}