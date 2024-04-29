package com.mccarty.ritmo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import com.mccarty.ritmo.repository.local.LocalRepository
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import kotlin.jvm.Throws

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val localRepository: LocalRepository,
) : ViewModel() {

    sealed class RecentlyPlayedMusicState {
        data object Pending: RecentlyPlayedMusicState()
        data class Success<T: RecentlyPlayedItem>(val data: T): RecentlyPlayedMusicState()
        data object  Error: RecentlyPlayedMusicState()
    }

    sealed class PlaylistState {
        data object Pending: PlaylistState()
        data class Success<T: PlaylistData.PlaylistItem>(val data: T): PlaylistState()
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

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

    private var _recentlyPlayedCached = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayedCached: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayedCached

    private var _playLists = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val playLists: StateFlow<List<PlaylistItem>> = _playLists

    private var _queueItemList = MutableStateFlow<List<CurrentQueueItem>>(emptyList())
    val queueItemList: StateFlow<List<CurrentQueueItem>> = _queueItemList

    private var _currentAlbum = MutableStateFlow(CurrentlyPlaying())
    val currentAlbum: StateFlow<CurrentlyPlaying> = _currentAlbum

    private var _currentAlbumImageUrl = MutableStateFlow("")
    val currentAlbumImageUrl: StateFlow<String> = _currentAlbumImageUrl

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _currentlyPlaying = MutableStateFlow(false)
    val currentlyPlaying: StateFlow<Boolean> = _currentlyPlaying

    private var _mainMusicHeader = MutableStateFlow(MainMusicHeader())
    val mainMusicHeader: StateFlow<MainMusicHeader> = _mainMusicHeader

    private var _hasInternetConnection = MutableLiveData<Boolean>()
    val hasInternetConnection: LiveData<Boolean> = _hasInternetConnection

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Pending)
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic.asStateFlow()

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending)
    val playlist: StateFlow<PlaylistState> = _playlist.asStateFlow()

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(LastPlayedSongState.Pending(true))
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong.asStateFlow()

    private var _currentlyPlayingTrack = MutableStateFlow<CurrentlyPayingTrackState>(CurrentlyPayingTrackState.Pending)
    val currentlyPlayingTrack: StateFlow<CurrentlyPayingTrackState> = _currentlyPlayingTrack.asStateFlow()

    fun getRecentlyPlayed() {
        viewModelScope.launch {
            repository.recentlyPlayed.stateIn(scope = viewModelScope)
                .first {
                    val list = processRecentlyPlayed(it)
                    println("MainViewModel ***** OG ${it.body()}")
                    _recentlyPlayed.value = list
                    //localRepository.insertRecentlyPlayedList(list) // TODO: changed model
                    true
                }
        }
    }

    // TODO: remove duplicate code
    fun getRecentlyPlayedForHeader(): List<TrackV2Item> {
        var list: List<TrackV2Item> = mutableListOf()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.recentlyPlayed.stateIn(scope = viewModelScope)
                    .first {
                        list = processRecentlyPlayed(it)
                        true
                    }
            }
        }
        return list
    }

    fun fetchPlaylist() {
        viewModelScope.launch {
            repository.fetchPlayList.collect {
                when(it){
                    is NetworkRequest.Error -> PlaylistState.Error
                    is NetworkRequest.Success -> {
                        _playlist.value = PlaylistState.Success(it.data)
                    }
                }
            }
        }
    }

    fun getCurrentlyPlaying() {
        viewModelScope.launch {
            repository.currentlyPlayingTrack.stateIn(scope = viewModelScope)
                .collect {
                    val playing = processCurrentlyPlaying(it)
                    val isPlaying = playing.first
                    val item = playing.second
                    _currentlyPlaying.value = isPlaying
                    if (isPlaying) {
                        _mainMusicHeader.value = MainMusicHeader().apply {
                            this.imageUrl = item.album.images[0].url
                            this.artistName = item.album.artists[0].name
                            this.albumName = item.album.name
                            this.songName = item.name
                        }
                    }
                }
        }
    }

    fun fetchCurrentlyPlaying() {
        viewModelScope.launch {
            repository.fetchCurrentlyPlayingTrack.collect {
                when (it) {
                    is NetworkRequest.Error -> CurrentlyPayingTrackState.Error
                    is NetworkRequest.Success -> {
                        CurrentlyPayingTrackState.Success(it.data)
                        val isPlaying = it.data.is_playing
                        val item = it.data.item
                        _currentlyPlaying.value = isPlaying
                        if (isPlaying) {
                            _mainMusicHeader.value = MainMusicHeader().apply {
                                this.imageUrl = item.album.images[0].url ?: ""
                                this.artistName = item.album.artists[0].name ?: ""
                                this.albumName = item.album.name ?: ""
                                this.songName = item.name ?: ""
                            }
                        }
                    }
                }
            }
        }
    }

    fun setMainHeader(isPlaying: Boolean, list: List<TrackV2Item>) {
        if(!isPlaying && list.isNotEmpty()) {
            _mainMusicHeader.value = MainMusicHeader().apply {
                this.imageUrl = list.get(0).track?.album?.images?.get(0)?.url ?: ""
                this.artistName = list.get(0).track?.album?.artists?.get(0)?.name ?: ""
                this.albumName = list.get(0).track?.album?.name ?: ""
                this.songName = list.get(0).track?.name ?: ""
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
                            is NetworkRequest.Success -> _lastPlayedSong.value = LastPlayedSongState.Success(it.data)
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
        //fetchCurrentlyPlaying()
        //fetchRecentlyPlayedMusic()
        fetchLastPlayedSong()
        //fetchPlaylist()
    }

    fun fetchRecentlyPlayedMusic() {
        _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Pending
        viewModelScope.launch {
            repository.recentlyPlayedMusic().catch {
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
}
