package com.mccarty.ritmo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.PlaylistItem
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem as RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.viewmodel.PlayerAction
import com.mccarty.ritmo.viewmodel.TrackSelectAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

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

    sealed class AllPlaylistsState {
        data object Pending: AllPlaylistsState()
        data class Success(val playLists: List<PlaylistData.Item>): AllPlaylistsState()
        data object  Error: AllPlaylistsState()
    }

    sealed class PlaylistState {
        data object Pending: PlaylistState()
        //data class Success(val playList: List<PlaylistItem>): PlaylistState()
        data class Success(val data: List<PlaylistItem>): PlaylistState()
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

    private val _albumId = MutableStateFlow("null")
    val albumId: StateFlow<String> = _albumId

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Pending)
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending)
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending)
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(LastPlayedSongState.Pending(true))
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong

    private var _musicHeader = MutableStateFlow(MusicHeader())
    val musicHeader: StateFlow<MusicHeader> = _musicHeader

    private var _artistName = MutableStateFlow<String?>(null)
    val artistName: StateFlow<String?> = _artistName

    private var _playerIsPaused = MutableStateFlow<Boolean>(true)
    val playerIsPaused: StateFlow<Boolean> = _playerIsPaused

    private var _trackUri = MutableStateFlow<String?>(null)
    val trackUri: StateFlow<String?> = _trackUri

    private var _playListItems = MutableStateFlow<Map<String,String>>(emptyMap<String, String>())
    val playListItems: StateFlow<Map<String,String>> = _playListItems

    fun fetchPlaylist() {
        viewModelScope.launch {
            repository.fetchPlayLists().collect {
                when(it){
                    is NetworkRequest.Error -> AllPlaylistsState.Error
                    is NetworkRequest.Success -> {
                        _allPlaylists.value = AllPlaylistsState.Success(it.data.items)
                    }
                }
            }
        }
    }

    fun fetchPlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.fetchPlayList(playlistId).collect {
                // TODO: handle pending
                when(it) {
                    is NetworkRequest.Error -> {
                        println("***** ${it.toString()}")
                        // TODO: handle error
                    }
                    is NetworkRequest.Success -> {
                        _playlist.value = PlaylistState.Success(it.data.items)
                    }
                }
            }
        }
    }

/*    fun fetchCurrentlyPlaying() {
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
    }*/

/*    fun fetchLastPlayedSong() {
        _lastPlayedSong.value = LastPlayedSongState.Pending(true)
        _recentlyPlayed.value.firstOrNull()?.track?.album?.id?.let { id ->
            viewModelScope.launch {
                repository.fetchAlbumInfo(id).collect {
                    when (it) {
                        is NetworkRequest.Error -> _lastPlayedSong.value = LastPlayedSongState.Error
                        is NetworkRequest.Success -> {
                            _lastPlayedSong.value = LastPlayedSongState.Success(it.data)
                        }
                    }
                }
            }
        } ?: run {
            _lastPlayedSong.value = LastPlayedSongState.Pending(false)
        }
    }*/

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

    fun setArtistName(name: String?) {
        if (name != null) {
            _artistName.value = name
        }
    }

    fun setCurrentlyPlayingState(isPaused: Boolean) {
        _playerIsPaused.value = isPaused
    }

    fun setTrackUri(trackUri: String?) {
        if (_trackUri.value != trackUri && _trackUri.value  != null) {
            _trackUri.value = trackUri
        }
    }

    fun playerAction(action: PlayerAction) {
        when(action) {
            PlayerAction.Back -> println("MainViewModel ***** BACK")
            PlayerAction.Pause -> println("MainViewModel ***** PAUSE")
            PlayerAction.Play -> println("MainViewModel ***** PLAY")
            is PlayerAction.Seek -> println("MainViewModel ***** SEEK")
            PlayerAction.Skip -> println("MainViewModel ***** SKIP")
        }
    }
}
