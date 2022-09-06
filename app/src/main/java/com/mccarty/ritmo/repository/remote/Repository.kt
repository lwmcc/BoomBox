package com.mccarty.ritmo.repository.remote

import android.app.Application
import androidx.compose.animation.core.FloatSpringSpec
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.codelab.android.datastore.AlbumPreference
import com.google.gson.JsonObject
import com.mccarty.ritmo.api.ApiService
import com.mccarty.ritmo.data.AlbumPreferenceSerializer
import com.mccarty.ritmo.model.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Inject

class Repository @Inject constructor(
    private val dispatchers: Dispatchers,
    private val retrofit: Retrofit
    ) {

    // TODO: pass in constructor?
    private val refreshInterval: Long = 5000

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
            delay(refreshInterval)
        }
    }

    val userQueue: Flow<Response<JsonObject>> = flow {
        while(true) {
            val queue = retrofit.create(ApiService::class.java).getUsersQueue()
            emit(queue)
            delay(refreshInterval)
        }
    }

    val currentlyPlaying: Flow<Response<JsonObject>> = flow {
        while(true) {
            val playing = retrofit.create(ApiService::class.java).getCurrentlyPlaying()
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