package com.mccarty.ritmo.api
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.payload.PlaybackState
import com.mccarty.ritmo.model.payload.Playlist
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/v1/me/playlists")
    suspend fun fetchPlayLists(): NetworkRequest<PlaylistData.PlaylistItem>

    @GET("/v1/playlists/{playlist_id}/tracks")
    suspend fun fetchPlayList(@Path("playlist_id") playlistIdd: String): NetworkRequest<Playlist>

    @GET("/v1/me/player/recently-played")
    suspend fun fetchRecentlyPlayedItem(): NetworkRequest<RecentlyPlayedItem>

    @GET("/v1/albums/{id}")
    suspend fun fetchAlbumInfo(@Path("id") id: String): NetworkRequest<AlbumXX>

    @GET("/v1/me/player/currently-playing")
    suspend fun fetchCurrentlyPlayingTrack(): NetworkRequest<CurrentlyPlayingTrack>

    @GET("/v1/me/player")
    suspend fun fetchPlaybackState(): NetworkRequest<PlaybackState>
}
