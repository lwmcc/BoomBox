package com.mccarty.ritmo.viewmodel

import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.MediaDetails
import com.mccarty.ritmo.domain.RemoteService
import com.mccarty.ritmo.domain.Ticker
import com.mccarty.networkrequest.network.NetworkRequest.Success as NetworkRequestSuccess
import com.mccarty.ritmo.domain.model.AlbumXX
import com.mccarty.ritmo.domain.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.domain.model.ExternalIds
import com.mccarty.ritmo.domain.model.ExternalUrlsX
import com.mccarty.ritmo.domain.model.Tracks
import com.mccarty.ritmo.domain.model.payload.Actions
import com.mccarty.ritmo.domain.model.payload.Album
import com.mccarty.ritmo.domain.model.payload.Context
import com.mccarty.ritmo.viewmodel.MainViewModel.RecentlyPlayedMusicState.Success as RecentlyPlayedMusicStateSuccess
import com.mccarty.ritmo.viewmodel.MainViewModel.AllPlaylistsState.Success as PlaylistStateSuccess
import com.mccarty.ritmo.domain.model.payload.Cursors
import com.mccarty.ritmo.domain.model.payload.Device
import com.mccarty.ritmo.domain.model.payload.ExternalIds as PayloadExternalIds
import com.mccarty.ritmo.domain.model.payload.ExternalUrls
import com.mccarty.ritmo.domain.model.payload.Item
import com.mccarty.ritmo.domain.model.payload.LinkedFrom
import com.mccarty.ritmo.domain.model.payload.PlaybackState
import com.mccarty.ritmo.domain.model.payload.Playlist
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.domain.model.payload.Restrictions
import com.mccarty.ritmo.domain.model.payload.Seeds
import com.mccarty.ritmo.domain.model.payload.Track
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.repository.remote.Repository
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineRule()

    @Test
    fun `assert instanceOf RecentlyPlayedMusicState Success`() = runTest {
        // Arrange
        val viewModel = MainViewModel(
            repository = RepositoryFake(),
            remoteService = RemoteServiceFake(),
            details = MediaDetailsFake(),
            sliderTicker = TickerFake(),
        )

        // Act
        viewModel.fetchRecentlyPlayedMusic()

        // Assert
        assertThat(
            viewModel.recentlyPlayedMusic.value,
            instanceOf(RecentlyPlayedMusicStateSuccess::class.java)
        )
    }

    @Test
    fun `assert instanceOf PlaylistState Success`() = runTest {
        // Arrange
        val viewModel = MainViewModel(
            repository = RepositoryFake(),
            remoteService = RemoteServiceFake(),
            details = MediaDetailsFake(),
            sliderTicker = TickerFake(),
        )

        // Act
        viewModel.fetchPlaylist("playlist_id")

        // Assert
        assertThat(viewModel.allPlaylists.value, instanceOf(PlaylistStateSuccess::class.java))
    }

    class RemoteServiceFake: RemoteService {
        override fun onTrackSelected(
            remote: SpotifyAppRemote?,
            action: TrackSelectAction.TrackSelect
        ) {

        }
    }

    class MediaDetailsFake: MediaDetails {
        override fun mediaDetails(tracks: List<Any>): List<Details> {
            return emptyList()
        }
    }

    class TickerFake: Ticker {
        override suspend fun getPlaybackPosition(
            position: Long,
            delay: Long,
            duration: Long
        ): Flow<Long> {
           return flow {
               emit(10L)
           }
        }
    }

    class RepositoryFake: Repository {
        val externalUrls = ExternalUrls(
            spotify = "spotifyString"
        )

        val context = Context(
            external_urls = externalUrls,
            href = "contextHref",
            type = "contextType",
            uri = "contextUri"
        )

        val album = Album(
            album_type = "albumType",
            artists = emptyList(),
            available_markets = emptyList(),
            external_urls = ExternalUrls(spotify = "spotify"),
            href = "albumHref",
            id = "1234",
            images = emptyList(),
            name = "albumName",
            release_date = "date",
            release_date_precision = "date",
            restrictions = Restrictions(reason = "some reasons"),
            total_tracks = 77,
            type = "albumType",
            uri = "albumUrl",
        )

        val track = Track(
            album = album,
            artists = emptyList(), //List<ArtistX>,
            available_markets = emptyList(),
            disc_number = 3,
            duration_ms = 30000,
            explicit = true,
            external_ids = PayloadExternalIds(
                ean = "ean",
                isrc = "isrc",
                upc = "upc"
            ),
            external_urls = ExternalUrls(spotify = "spotify"),
            href = "trackHref",
            id = "trackId",
            is_local = false,
            is_playable = true,
            linked_from = LinkedFrom(),
            name = "trackName",
            popularity = 66,
            preview_url = "trackPreviewUrl",
            restrictions = Restrictions(reason = "some reasons"),
            track_number = 33,
            type = "trackType",
            uri = "trackUri"
        )

        val item = Item(
            context = context,
            played_at = "playedAt",
            track = track,
        )

        val device = Device(
            id = "",
            is_active = true,
            is_private_session = true,
            is_restricted = false,
            name = "",
            supports_volume = true,
            type = "",
            volume_percent = 10,
        )

        val actions = Actions(
            interrupting_playback = false,
            pausing = false,
            resuming = false,
            seeking = false,
            skipping_next = false,
            skipping_prev = false,
            toggling_repeat_context = false,
            toggling_repeat_track = false,
            toggling_shuffle = false,
            transferring_playback = false,
        )

        val playbackState = PlaybackState(
            actions = actions,
            context = context,
            currently_playing_type = "",
            device = device,
            is_playing = true,
            item = item,
            progress_ms = 0,
            repeat_state = "",
            shuffle_state = false,
            timestamp = 10L,
        )

        val playList = Playlist(
            href = "",
            items = emptyList(),
            limit = 50,
            next = "",
            offset = 1,
            previous = "",
            total = 1,
        )

        override suspend fun fetchRecentlyPlayedItem(): Flow<com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>> {
            return flow {
                emit(
                    NetworkRequestSuccess(
                        RecentlyPlayedItem(
                            cursors = Cursors(
                                after = "",
                                before = "",
                            ),
                            href = "",
                            items = emptyList(),
                            limit = 1,
                            next = "",
                            total = 1,
                        )
                    )
                )
            }
        }

        override suspend fun fetchAlbumInfo(id: String): Flow<com.mccarty.networkrequest.network.NetworkRequest<AlbumXX>> {
            return flow {
                emit(
                    NetworkRequestSuccess(
                        AlbumXX(
                            album_type = "LP",
                            artists = emptyList(), // : List<Artist>
                            available_markets = emptyList(), //  List<String>
                            copyrights = emptyList(), //  List<Copyright>
                            external_ids = ExternalIds(
                                isrc = "isrc",
                            ),
                            external_urls = ExternalUrlsX(spotify = "some string"),
                            genres = emptyList(), // List<Any>
                            href = "https://www.google.com",
                            id = "007",
                            images = emptyList(), // : List<Image>
                            label = "Intersxope",
                            name = "Simply The Best",
                            popularity = 5,
                            release_date = "",
                            release_date_precision = "",
                            total_tracks = 10,
                            tracks = Tracks(
                                href = "some tracks",
                                total = 10,
                            ),
                            type = "album",
                            uri = "https://www.google.com",
                        )
                    )
                )
            }
        }

        override suspend fun fetchCurrentlyPlayingTrack(): Flow<com.mccarty.networkrequest.network.NetworkRequest<CurrentlyPlayingTrack>> {
            return flow { }
        }

        override suspend fun fetchPlayLists(): Flow<com.mccarty.networkrequest.network.NetworkRequest<PlaylistData.PlaylistItem>> {
            return flow {
                emit(
                    NetworkRequestSuccess(
                        PlaylistData.PlaylistItem(
                            href = "",
                            items = emptyList(),
                            limit = 1,
                            next = "",
                            offset = 1,
                            previous = "",
                            total = 1,
                        )
                    )
                )
            }
        }

        override suspend fun fetchUserPlayList(playlistId: String): Flow<NetworkRequest<Playlist>> {
            return flow {
                emit(NetworkRequest.Success(playList))
            }
        }

        override suspend fun fetchPlaybackState(): Flow<NetworkRequest<PlaybackState>> {
            return flowOf(NetworkRequest.Success(playbackState))
        }

        override suspend fun fetchRecommendedPlaylists(
            trackIds: String,
            artistIds: String
        ): Flow<NetworkRequest<Seeds>> {
            return flow {
                emit(
                    NetworkRequest.Success(
                        Seeds(
                            seeds = emptyList(),
                            tracks = emptyList(),
                        )
                    )
                )
            }
        }
    }
}
