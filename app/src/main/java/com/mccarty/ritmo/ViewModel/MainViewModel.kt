package com.mccarty.ritmo.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.getImageUrlFromList
import com.mccarty.ritmo.utils.processPlaylist
import com.mccarty.ritmo.utils.processQueue
import com.mccarty.ritmo.utils.processRecentlyPlayed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val dispatchers: Dispatchers,
    ): ViewModel() {

    private var _recentlyPlayed = MutableStateFlow<List<RecentlyPlayedItem>>(emptyList())
    val recentlyPlayed: StateFlow<List<RecentlyPlayedItem>> = _recentlyPlayed

    private var _playLists = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val playLists: StateFlow<List<PlaylistItem>> = _playLists

    private var _queueItemList = MutableStateFlow<List<CurrentQueueItem>>(emptyList())
    val queueItemList: StateFlow<List<CurrentQueueItem>> = _queueItemList

    private var _currentAlbum = MutableStateFlow(CurrentAlbum())
    val currentAlbum: StateFlow<CurrentAlbum> = _currentAlbum

    private var _currentAlbumImageUrl = MutableStateFlow("larry")
    val currentAlbumImageUrl: StateFlow<String> = _currentAlbumImageUrl

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
                        println("IMAEG $image")
                    }

                    //_currentAlbumImageUrl.value = getAlbumArtwork(pair.first.album?.images)
                }
        }
    }

    fun setAuthToken(token: String) {
        ApiClient.apply {
            this.token = token
        }
    }

    private fun getAlbumArtwork(images: List<Image>?): String {
        var image = ""
        image = images?.let {
            if(it.isNotEmpty()) {
                it[0].url
            } else {
                ""
            }
        }.toString()
        return image
    }
}