package com.mccarty.ritmo.repository.remote

import com.google.gson.JsonObject
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

open class MusicRepository @Inject constructor(private val retrofit: Retrofit): Repository {

    val recentlyPlayed: Flow<Response<JsonObject>> = flow {
        val recentlyPlayed = retrofit.create(ApiService::class.java)
            .getRecentlyPlayedTracks() // TODO: don't create this twice
        emit(recentlyPlayed)
    }

    override suspend fun fetchRecentlyPlayedItem(): Flow<NetworkRequest<RecentlyPlayedItem>>  = flow {
        emit(retrofit.create(ApiService::class.java).fetchRecentlyPlayedItem())
    }

    override suspend fun fetchAlbumInfo(id: String): Flow<NetworkRequest<AlbumXX>> = flow {
        emit(retrofit.create(ApiService::class.java).fetchAlbumInfo(id))
    }

    override suspend fun fetchCurrentlyPlayingTrack(): Flow<NetworkRequest<CurrentlyPlayingTrack>> = flow {
        emit(retrofit.create(ApiService::class.java).fetchCurrentlyPlayingTrack())
    }

    override suspend fun fetchPlayList(): Flow<NetworkRequest<PlaylistData.PlaylistItem>> = flow {
        emit(retrofit.create(ApiService::class.java).fetchPlayList())
    }
}
