package com.mccarty.ritmo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import kotlin.jvm.Throws

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
) : ViewModel() {

    sealed class RecentlyPlayedMusicState {
        data object Pending: RecentlyPlayedMusicState()
        data class Success<T: RecentlyPlayedItem>(val data: T): RecentlyPlayedMusicState()
        data object  Error: RecentlyPlayedMusicState()
    }

    sealed class Recently {
        data object Pending: Recently()
        data class Success(val success: String): Recently()
        data object  Error: Recently()
    }

    sealed class PlaylistState {
        data object Pending: PlaylistState()
        data class Success(val playList:  List<PlaylistData.Item>): PlaylistState()
        data object  Error: PlaylistState()
    }

    sealed class LastPlayedSongState {
        data class Pending(val pending: Boolean): LastPlayedSongState()
        data class Success<T: AlbumXX>(val data: T): LastPlayedSongState()
        data object  Error: LastPlayedSongState()
    }

    sealed class CurrentlyPayingTrackState {
        data object Pending: CurrentlyPayingTrackState()
        data class Success<T: CurrentlyPlayingTrack>(val data: T): CurrentlyPayingTrackState()
        data object  Error: CurrentlyPayingTrackState()
    }

    //private val trackId: String = checkNotNull(savedStateHandle["id"])

    private val _albumId = MutableStateFlow("null")
    val albumId: StateFlow<String> = _albumId

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

/*    private var _recentlyPlayedItem = MutableStateFlow<List<Item>>(emptyList())
    val recentlyPlayedItem: StateFlow<List<Item>> = _recentlyPlayedItem

    private var _recentlyPlayedCached = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayedCached: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayedCached

    private var _queueItemList = MutableStateFlow<List<CurrentQueueItem>>(emptyList())
    val queueItemList: StateFlow<List<CurrentQueueItem>> = _queueItemList

    private var _currentAlbum = MutableStateFlow(CurrentlyPlaying())
    val currentAlbum: StateFlow<CurrentlyPlaying> = _currentAlbum*/

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Pending)
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending)
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(LastPlayedSongState.Pending(true))
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong

    private var _musicHeader = MutableStateFlow(MusicHeader())
    val musicHeader: StateFlow<MusicHeader> = _musicHeader

    private fun fetchPlaylist() {
        viewModelScope.launch {
            repository.fetchPlayList().collect {
                when(it){
                    is NetworkRequest.Error -> PlaylistState.Error
                    is NetworkRequest.Success -> {
                        _playlist.value = PlaylistState.Success(it.data.items)
                    }
                }
            }
        }
    }

    private fun fetchCurrentlyPlaying() {
        viewModelScope.launch {
            repository.fetchCurrentlyPlayingTrack().collect {
                when (it) {
                    is NetworkRequest.Error -> CurrentlyPayingTrackState.Error
                    is NetworkRequest.Success -> {
                        _albumId.value = it.data.item.album.id
                        CurrentlyPayingTrackState.Success(it.data)
                    }
                }
            }
        }
    }

    fun fetchLastPlayedSong() {
        _lastPlayedSong.value = LastPlayedSongState.Pending(true)
        if (_recentlyPlayed.value.isNotEmpty()) {
            _recentlyPlayed.value[0].track?.album?.id?.let { id ->
                viewModelScope.launch {
                    repository.fetchAlbumInfo(id).collect {
                        when(it) {
                            is NetworkRequest.Error -> _lastPlayedSong.value = LastPlayedSongState.Error
                            is NetworkRequest.Success -> {
                                _lastPlayedSong.value = LastPlayedSongState.Success(it.data)
                            }
                        }
                    }
                }
            }
        } else {
            _lastPlayedSong.value = LastPlayedSongState.Pending(false)
        }
    }

    @Throws(IOException::class)
    fun setAuthToken(context: MainActivity, token: String) {
        ApiClient.apply {
            this.context = context
            this.token = token
        }
        fetchCurrentlyPlaying()
        fetchRecentlyPlayedMusic()
        fetchLastPlayedSong()
        fetchPlaylist()
    }

    fun fetchRecentlyPlayedMusic() {
        _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Pending
        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Error
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Error
                    is NetworkRequest.Success -> {
                        _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(it.data)
                    }
                }
            }
        }
    }

    fun setMusicHeader(header: MusicHeader) {
        _musicHeader.value = header
    }
}
