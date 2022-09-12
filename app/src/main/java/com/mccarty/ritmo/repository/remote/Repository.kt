package com.mccarty.ritmo.repository.remote

import com.google.gson.JsonObject
import com.mccarty.ritmo.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit
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
        while(true) {
            val recentlyPlayed = retrofit.create(ApiService::class.java).getRecentlyPlayedTracks()
            emit(recentlyPlayed)
            delay(refreshInterval)
        }
    }

    val playLists: Flow<Response<JsonObject>> = flow {
        while(true) {
            val playLists = retrofit.create(ApiService::class.java).getUserPlaylists()
            emit(playLists)
            delay(tenMinuteInterval)
        }
    }

    val userQueue: Flow<Response<JsonObject>> = flow {
        while(true) {
            val queue = retrofit.create(ApiService::class.java).getUsersQueue()
            emit(queue)
            delay(fiveMinuteInterval)
        }
    }

    val currentlyPlayingTrack: Flow<Response<JsonObject>> = flow {
        while(true) {
            val playing = retrofit.create(ApiService::class.java).getCurrentlyPlayingTrack()
            emit(playing)
            delay(refreshInterval)
        }
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