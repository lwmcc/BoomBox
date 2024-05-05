package com.mccarty.ritmo.viewmodel

import com.mccarty.networkrequest.network.NetworkRequest.Success as NetworkRequestSuccess
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.ExternalIds
import com.mccarty.ritmo.model.ExternalUrlsX
import com.mccarty.ritmo.model.Tracks
import com.mccarty.ritmo.MainViewModel.RecentlyPlayedMusicState.Success as RecentlyPlayedMusicStateSuccess
import com.mccarty.ritmo.model.payload.Cursors
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.MusicRepository
import com.mccarty.ritmo.repository.remote.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineRule()

    @Test
    fun `assert instanceOf RecentlyPlayedMusicState`() = runTest {
        val mockRepository =  mock(MusicRepository::class.java)
        val repositoryFake = RepositoryFake()
        val viewModel = MainViewModel(mockRepository, repositoryFake)

        viewModel.fetchRecentlyPlayedMusic()

        assertThat(viewModel.recentlyPlayedMusic.value, instanceOf(RecentlyPlayedMusicStateSuccess::class.java))
    }

    @Test
    fun `assert instanceOf LastPlayedSongState`() = runTest {
        val mockRepository =  mock(MusicRepository::class.java)
        val repositoryFake = RepositoryFake()

        val viewModel = MainViewModel(mockRepository, repositoryFake)
        viewModel.fetchLastPlayedSong()

        assertThat(viewModel.lastPlayedSong.value, instanceOf(MainViewModel.LastPlayedSongState.Success::class.java))
    }

    class RepositoryFake: Repository {
        override suspend fun fetchRecentlyPlayedMusic(): Flow<com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>> {
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

        override suspend fun fetchRecentlyPlayedItem(): Flow<com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>> {
            TODO("Not yet implemented")
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
                            external_ids = ExternalIds(isrc = "some irc"),
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
            TODO("Not yet implemented")
        }

        override suspend fun fetchPlayList(): Flow<com.mccarty.networkrequest.network.NetworkRequest<PlaylistData.PlaylistItem>> {
            TODO("Not yet implemented")
        }
    }
}
