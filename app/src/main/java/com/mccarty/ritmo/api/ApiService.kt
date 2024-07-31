package com.mccarty.ritmo.api
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.domain.model.AlbumXX
import com.mccarty.ritmo.domain.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.domain.model.payload.PlaybackState
import com.mccarty.ritmo.domain.model.payload.Playlist
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.domain.model.payload.Seeds
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/v1/me/playlists")
    suspend fun fetchPlayLists(): NetworkRequest<PlaylistData.PlaylistItem>

    @GET("/v1/playlists/{playlist_id}/tracks")
    suspend fun fetchPlayList(@Path("playlist_id") playlistIdd: String): NetworkRequest<Playlist>

    @GET("/v1/playlists/{playlist_id}/tracks")
    suspend fun fetchUserPlayList(@Path("playlist_id") playlistIdd: String): NetworkRequest<Playlist>

    @GET("/v1/me/player/recently-played?limit=50")
    suspend fun fetchRecentlyPlayedItem(): NetworkRequest<RecentlyPlayedItem>

    @GET("/v1/albums/{id}")
    suspend fun fetchAlbumInfo(@Path("id") id: String): NetworkRequest<AlbumXX>

    @GET("/v1/me/player/currently-playing")
    suspend fun fetchCurrentlyPlayingTrack(): NetworkRequest<CurrentlyPlayingTrack>

    @GET("/v1/me/player")
    suspend fun fetchPlaybackState(): NetworkRequest<PlaybackState>

    @GET("/v1/recommendations?limit=100")
    suspend fun fetchRecommendedPlaylist(
        @Query("seed_tracks") seed_tracks: String,
        @Query("seed_artists") seed_artists: String,
    ): NetworkRequest<Seeds>
}
