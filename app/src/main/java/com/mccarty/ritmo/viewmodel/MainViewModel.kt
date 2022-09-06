package com.mccarty.ritmo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private var _recentlyPlayed = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayed

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

    // Last played data
    private var _artistName = MutableLiveData<String>("")
    var artistName: LiveData<String> = _artistName

    private var _albumName = MutableLiveData<String>("")
    var albumName: LiveData<String> = _albumName

    private var _imageUrl = MutableLiveData<String>("")
    var imageUrl: LiveData<String> = _imageUrl

    private var _releaseDate = MutableLiveData<String>("")
    var releaseDate: LiveData<String> = _releaseDate

    fun getRecentlyPlayed() {
        viewModelScope.launch {
            repository.recentlyPlayed.stateIn(scope = viewModelScope)
                .collect {
                    _recentlyPlayed.value = processRecentlyPlayed(it)
                }
        }
    }

    fun getPlaylists() {
        viewModelScope.launch {
            repository.playLists.stateIn(scope = viewModelScope)
                .collect {
                    _playLists.value = processPlaylist(it)
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

    fun getCurrentlyPlaying() {
        viewModelScope.launch {
            repository.currentlyPlaying.stateIn(scope = viewModelScope)
                .collect {
                    _currentlyPlaying.value = processCurrentlyPlaying(it)!! // This will always return something
                    println("MainViewModel ${_currentlyPlaying.value}")
                }
        }
    }

    fun getLastPlayedSongId() {
        if (_recentlyPlayed.value.isNotEmpty()) {
            val id = _recentlyPlayed.value[0].track.album.id
            viewModelScope.launch {
                repository.getAlbumInfo(id).stateIn(scope = viewModelScope)
                    .collect {
                        _album.value = processAlbumData(it)
                    }
            }
        }
    }

    fun setAuthToken(token: String) {
        ApiClient.apply {
            this.token = token
        }
    }

    fun setLastPlayedAlbumData(artistName: String, albumName: String, imageUrl: String, releaseDate: String) {
        _artistName.value = artistName
        _albumName.value = albumName
        _imageUrl.value = imageUrl
        _releaseDate.value = releaseDate
    }
}