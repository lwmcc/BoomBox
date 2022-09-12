package com.mccarty.ritmo

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
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

    private var _recentlyPlayed = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayed

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
                .first {
                    println("MainViewModel ${it.body()}")
                    val pair = processRecentlyPlayed(it)
                    _recentlyPlayed.value = pair.second
                    viewModelScope.launch(Dispatchers.IO) {
                        localRepository.insertRecentlyPlayedList(pair.second)
                    }
                    true
                }
        }
    }

    fun getPlaylists() {
        viewModelScope.launch {
            repository.playLists.stateIn(scope = viewModelScope)
                .collect {
                    _playLists.value = processPlaylist(it)

                    // TODO: save to db
                    // save interval??
                }
        }
    }

    fun getCurrentlyPlaying() {
        viewModelScope.launch {
            repository.currentlyPlaying.stateIn(scope = viewModelScope)
                .collect {
                    val playing = processCurrentlyPlaying(it)!! // This will always return something
                    _currentlyPlaying.value = playing

                    // image_url
                    // artist
                    // album
                    // song title
                    // release date

                    if(playing) {
                        // if true add to db
                        // show in UI
                    } else {
                        // else if false
                        //get from db
                        // show in ui

                        // if no song in db
                        // show default
                    }
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

    fun setLastPlayedAlbumData(
        artistName: String,
        albumName: String,
        imageUrl: String,
        releaseDate: String
    ) {
        _artistName.value = artistName
        _albumName.value = albumName
        _imageUrl.value = imageUrl
        _releaseDate.value = releaseDate
    }

    suspend fun retryIntervalHasExpired(): Boolean {
        val currentTime = System.currentTimeMillis() % 60
        var insertionTime = 0L
        var interval = 0
        viewModelScope.launch(Dispatchers.Default) {
            localRepository.getInsertionTimeSeconds().collect {
                insertionTime = it
            }
            localRepository.getRetryIntervalSeconds().collect {
                interval = it
            }
        }
        return currentTime - insertionTime > interval
    }

    suspend fun getRecentlyPlayedFromRepo() = localRepository.getRecentlyPlayed().collect {
        viewModelScope.launch(Dispatchers.Default) {
            val recentlyPlayed = mutableListOf<RecentlyPlayedItem>()
            it.forEach {
                println("MainViewModel ${it.track?.name}")
                recentlyPlayed.add(it)
            }
            //_recentlyPlayed.value = recentlyPlayed
        }
    }
}