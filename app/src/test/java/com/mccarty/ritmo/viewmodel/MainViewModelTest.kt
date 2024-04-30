package com.mccarty.ritmo.viewmodel

import android.net.NetworkRequest
import com.mccarty.networkrequest.network.NetworkRequest.Success as NetworkRequestSuccess
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.MainViewModel.RecentlyPlayedMusicState.Success as RecentlyPlayedMusicStateSuccess
import com.mccarty.ritmo.model.payload.Cursors
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.repository.remote.RepositoryInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineRule()

    val recentlyPlayedItem = RecentlyPlayedItem(
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

    @Test
    fun `assert playlist`() = runTest {
        val mockRepository =  mock(Repository::class.java)
        val repositoryFake = RepositoryFake()
        val viewModel = MainViewModel(mockRepository, repositoryFake)

        viewModel.fetchRecentlyPlayedMusic()
        repositoryFake.emit(NetworkRequestSuccess(recentlyPlayedItem))

        assertThat(viewModel.recentlyPlayedMusic.value, instanceOf(RecentlyPlayedMusicStateSuccess::class.java))
    }

    class RepositoryFake: RepositoryInt {
        private val flow = MutableSharedFlow<com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>>()
        suspend fun emit(value: com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>) = flow.emit(value)
        override suspend fun recentlyPlayedMusic(): Flow<com.mccarty.networkrequest.network.NetworkRequest<RecentlyPlayedItem>> = flow
    }
}
