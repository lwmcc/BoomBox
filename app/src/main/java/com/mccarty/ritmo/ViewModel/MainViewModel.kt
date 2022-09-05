package com.mccarty.ritmo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private var _recentlyPlayed = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayed

    private var _lastPlayed = MutableStateFlow<String>("")
    val lastPlayed: StateFlow<String> = _lastPlayed

    private var _playLists = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val playLists: StateFlow<List<PlaylistItem>> = _playLists

    private var _queueItemList = MutableStateFlow<List<CurrentQueueItem>>(emptyList())
    val queueItemList: StateFlow<List<CurrentQueueItem>> = _queueItemList

    // TODO: change name
    private var _currentAlbum = MutableStateFlow(CurrentlyPlaying())
    val currentAlbum: StateFlow<CurrentlyPlaying> = _currentAlbum

    private var _currentAlbumImageUrl = MutableStateFlow("")
    val currentAlbumImageUrl: StateFlow<String> = _currentAlbumImageUrl

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    fun getRecentlyPlayed() {
        viewModelScope.launch {
            repository.recentlyPlayed.stateIn(scope = viewModelScope)
                .collect {
                    _recentlyPlayed.value = processRecentlyPlayed(it)
                    println("MainViewModel THE ID ${_recentlyPlayed.value[0].track.album.id}")
            }
        }
    }

    fun getPlaylists() {
        viewModelScope.launch {
            repository.playLists.stateIn(scope = viewModelScope)
                .collect {
                    _playLists.value =  processPlaylist(it)
                }
        }
    }

    fun getQueue() {
        viewModelScope.launch {
            repository.userQueue.stateIn(scope = viewModelScope)
                .collect {
                    val pair = processQueue(it)
                    _currentAlbum.value = pair.first
                    _queueItemList.value = pair.second

                    val image = pair.first.album?.images?.getImageUrlFromList(0)

                    if (image != null) {
                        _currentAlbumImageUrl.value = image
                    }

                }
        }
    }

    // Track ID 3aDFhZOz4EkitF1DlYGsxM
    // _recentlyPlayed.value[0].track.album.id
    fun getLastPlayedSongId() {

        viewModelScope.launch {
            repository.getAlbumInfo("3aDFhZOz4EkitF1DlYGsxM").stateIn(scope = viewModelScope)
                .collect {
                    _album.value = processAlbumData(it)
                    println("MainViewModel GET ID ${_album.value.images.size}")
                    //println("MainViewModel GET ID ${_album.value.images[0].url}")
                }
        }
    }

    fun setAuthToken(token: String) {
        ApiClient.apply {
            this.token = token
        }
    }
}