package com.mccarty.ritmo.repository.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.NullPointerException
import javax.inject.Inject

class Repository @Inject constructor(
    private val dispatchers: Dispatchers,
    private val retrofit: Retrofit
    ) {

    // TODO: pass in constructor?
    private val refreshInterval: Long = 5000
    private val twentySecondInterval: Long = 20_000
    private val fiveMinuteInterval: Long = 300_000
    private val tenMinuteInterval: Long = 600_000

    val recentlyPlayed: Flow<Response<JsonObject>> = flow {
        val recentlyPlayed = retrofit.create(ApiService::class.java)
            .getRecentlyPlayedTracks() // TODO: don't create this twice
        emit(recentlyPlayed)
    }

    suspend fun recentlyPlayedMusic(): Flow<NetworkRequest<RecentlyPlayedItem>>  = flow {
        emit(retrofit.create(ApiService::class.java).fetchRecentlyPlayedTracks())
    }

    val playLists: Flow<Response<JsonObject>> = flow {
            emit(retrofit.create(ApiService::class.java).getUserPlaylists())
    }

    val playList: Flow<NetworkRequest<PlaylistData.Playlist>> = flow {
        emit(retrofit.create(ApiService::class.java).fetchUserPlaylist())
    }

    val userQueue: Flow<Response<JsonObject>> = flow {
        while(true) {
            val queue = retrofit.create(ApiService::class.java).getUsersQueue()
            emit(queue)
            delay(fiveMinuteInterval)
        }
    }

    val currentlyPlayingTrack: Flow<Response<JsonObject>> = flow {
        emit(retrofit.create(ApiService::class.java).getCurrentlyPlayingTrack())
    }

    val currentlyPlayingTrack2: Flow<NetworkRequest<Any>> = flow {
        emit(retrofit.create(ApiService::class.java).getCurrentlyPlayingTrack2())
    }

    fun getAlbumInfo(id: String): Flow<Response<JsonObject>> {
        return flow {
            while(true) {
                val album = retrofit.create(ApiService::class.java).getAlbum(id)
                emit(album)
                delay(refreshInterval)
            }
        }
    }
}