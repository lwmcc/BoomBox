package com.mccarty.ritmo.api

import com.google.gson.JsonObject
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    //@GET("/v1/me/playlists")
    //suspend fun getUserPlaylists(): Response<JsonObject>

    @GET("/v1/me/playlists")
    suspend fun fetchPlayList(): NetworkRequest<PlaylistData.PlaylistItem>

    @GET("/v1/me/player/recently-played")
    suspend fun getRecentlyPlayedTracks(): Response<JsonObject>

    @GET("/v1/me/player/recently-played")
    suspend fun fetchRecentlyPlayedItem(): NetworkRequest<RecentlyPlayedItem>

    @GET("/v1/me/player/queue")
    suspend fun getUsersQueue(): Response<JsonObject>

    @GET("/v1/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Response<JsonObject>

    @GET("/v1/albums/{id}")
    suspend fun fetchAlbumInfo(@Path("id") id: String): NetworkRequest<AlbumXX>

/*    @GET("/v1/me/player/currently-playing")
    suspend fun getCurrentlyPlayingTrack(): Response<JsonObject>*/

    @GET("/v1/me/player/currently-playing")
    suspend fun fetchCurrentlyPlayingTrack(): NetworkRequest<CurrentlyPlayingTrack>
}