package com.mccarty.ritmo.api

import com.google.gson.JsonObject
import com.mccarty.networkrequest.network.NetworkRequest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/v1/me/playlists")
    suspend fun getUserPlaylists(): Response<JsonObject>

    @GET("/v1/me/player/recently-played")
    suspend fun getRecentlyPlayedTracks(): Response<JsonObject>

    @GET("/v1/me/player/recently-played")
    suspend fun fetchRecentlyPlayedTracks(): NetworkRequest<Any>

    @GET("/v1/me/player/queue")
    suspend fun getUsersQueue(): Response<JsonObject>

    @GET("/v1/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Response<JsonObject>

    @GET("/v1/me/player/currently-playing")
    suspend fun getCurrentlyPlayingTrack(): Response<JsonObject>
}