package com.mccarty.ritmo.ViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.CurrentAlbum
import com.mccarty.ritmo.model.CurrentQueueItem
import com.mccarty.ritmo.model.PlaylistItem
import com.mccarty.ritmo.model.RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.processPlaylist
import com.mccarty.ritmo.utils.processQueue
import com.mccarty.ritmo.utils.processRecentlyPlayed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
                }
        }
    }

    fun setAuthToken(token: String) {
        ApiClient.apply {
            this.token = token
        }
    }
}