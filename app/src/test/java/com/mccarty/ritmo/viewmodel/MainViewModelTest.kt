package com.mccarty.ritmo.viewmodel

import android.net.NetworkRequest
import com.mccarty.networkrequest.network.NetworkRequest.Success as NetworkRequestSuccess
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.Artist
import com.mccarty.ritmo.model.Copyright
import com.mccarty.ritmo.model.ExternalIds
import com.mccarty.ritmo.model.ExternalUrlsX
import com.mccarty.ritmo.model.Image
import com.mccarty.ritmo.model.RecentlyPlayedTrack
import com.mccarty.ritmo.model.Tracks
import com.mccarty.ritmo.MainViewModel.RecentlyPlayedMusicState.Success as RecentlyPlayedMusicStateSuccess
import com.mccarty.ritmo.model.payload.Cursors
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.repository.remote.RepositoryInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineRule()

    @Test
    fun `assert instanceOf RecentlyPlayedMusicState`() = runTest {
        val mockRepository =  mock(Repository::class.java)
        val repositoryFake = RepositoryFake()
        val viewModel = MainViewModel(mockRepository, repositoryFake)

        viewModel.fetchRecentlyPlayedMusic()

        assertThat(viewModel.recentlyPlayedMusic.value, instanceOf(RecentlyPlayedMusicStateSuccess::class.java))
    }

    @Test
    fun `assert instanceOf LastPlayedSongState`() = runTest {
        val mockRepository =  mock(Repository::class.java)
        val repositoryFake = RepositoryFake()

        val viewModel = MainViewModel(mockRepository, repositoryFake)
        viewModel.fetchLastPlayedSong()

        assertThat(viewModel.lastPlayedSong.value, instanceOf(MainViewModel.LastPlayedSongState.Success::class.java))
    }

    class RepositoryFake: RepositoryInt {
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

        override suspend fun fetchRecentlyPlayedTracks(): Flow<com.mccarty.networkrequest.network.NetworkRequest<List<RecentlyPlayedTrack>>> {
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
    }
}
