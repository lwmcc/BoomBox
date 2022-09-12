package com.mccarty.ritmo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.repository.local.LocalRepository
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val localRepository: LocalRepository,
) : ViewModel() {

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

    fun getRecentlyPlayed() {
        viewModelScope.launch {
            repository.recentlyPlayed.stateIn(scope = viewModelScope)
                .first {
                    val list = processRecentlyPlayed(it)
                    _recentlyPlayed.value = list
                    //viewModelScope.launch(Dispatchers.IO) {
                    //    localRepository.insertRecentlyPlayedList(list) // TODO: changed model
                    //}
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

    fun getPlaylists() {
        viewModelScope.launch {
            repository.playLists.stateIn(scope = viewModelScope)
                .collect {
                    _playLists.value = processPlaylist(it)
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

    fun getLastPlayedSongId() {
        if (_recentlyPlayed.value.isNotEmpty()) {
            val id = _recentlyPlayed.value[0].track?.album?.id
            viewModelScope.launch {
                if (id != null) {
                    repository.getAlbumInfo(id).stateIn(scope = viewModelScope)
                        .collect {
                            _album.value = processAlbumData(it)
                        }
                }
            }
        }
    }

    fun setAuthToken(token: String) {
        ApiClient.apply {
            this.token = token
        }
    }
}