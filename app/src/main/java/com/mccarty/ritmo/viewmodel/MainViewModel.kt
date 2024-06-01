package com.mccarty.ritmo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.model.TrackV2Item
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.RecentlyPlayedItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.createTrackDetailsFromItems
import com.mccarty.ritmo.utils.createTrackDetailsFromPlayListItems
import com.mccarty.ritmo.viewmodel.PlayerAction
import com.mccarty.ritmo.viewmodel.TrackSelectAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    sealed class RecentlyPlayedMusicState {
        data class Success(val trackDetails: List<TrackDetails> = emptyList()): RecentlyPlayedMusicState()
    }

    sealed class MainMusicState {
        data object Pending: MainMusicState()
        data class Success(
            val trackDetails: List<TrackDetails> = emptyList(),
            val playLists: List<PlaylistData.Item> = emptyList(),
            ): MainMusicState()
        data object  Error: MainMusicState()
    }

    sealed class Recently {
        data object Pending: Recently()
        data class Success(val success: String): Recently()
        data object  Error: Recently()
    }

    sealed class AllPlaylistsState {
        data class Pending(val pending: Boolean): AllPlaylistsState()
        data class Success(val playLists: List<PlaylistData.Item>): AllPlaylistsState()
        data object  Error: AllPlaylistsState()
    }

    sealed class PlaylistState {
        data class Pending(val pending: Boolean): PlaylistState()

        data class Success(val trackDetails: List<TrackDetails>): PlaylistState()
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

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Success(emptyList()))
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic
    //private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Pending)
    //val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _playLists = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playLists: StateFlow<PlaylistState> = _playLists


    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending(true))
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _playlistTracks = MutableStateFlow<List<TrackDetails>>(emptyList())
    val playlistTracks: StateFlow<List<TrackDetails>> = _playlistTracks

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(LastPlayedSongState.Pending(true))
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong

    private var _musicHeader = MutableStateFlow(MusicHeader())
    val musicHeader: StateFlow<MusicHeader> = _musicHeader

    //private var _artistName = MutableStateFlow<String?>(null)
    //val artistName: StateFlow<String?> = _artistName

    private var _playerIsPaused = MutableStateFlow<Boolean>(true)
    val playerIsPaused: StateFlow<Boolean> = _playerIsPaused

    private var _trackUri = MutableStateFlow<String?>(null)
    val trackUri: StateFlow<String?> = _trackUri

    private suspend fun fetchAllPlaylists() {
        AllPlaylistsState.Pending(true)
        repository.fetchPlayLists().collect {
            when (it) {
                is NetworkRequest.Error -> AllPlaylistsState.Error
                is NetworkRequest.Success -> {
                    _allPlaylists.value = AllPlaylistsState.Success(it.data.items)
                }
            }
        }
        AllPlaylistsState.Pending(false)
    }

    fun fetchPlaylist(playlistId: String) {
        PlaylistState.Pending(true)
        viewModelScope.launch {
            repository.fetchPlayList(playlistId).collect {
                when(it) {
                    is NetworkRequest.Error -> {
                        println("***** ${it.toString()}")
                        // TODO: handle error
                    }
                    is NetworkRequest.Success -> {
                        _playLists.value = PlaylistState.Success(it.data.items.createTrackDetailsFromPlayListItems())
                    }
                }
            }
        }
        PlaylistState.Pending(false)
    }

    fun fetchRecentlyPlayedMusic() {
        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(emptyList())
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> _recentlyPlayedMusic.value =
                        RecentlyPlayedMusicState.Success(emptyList())
                    is NetworkRequest.Success -> {
                        _recentlyPlayedMusic.value =
                            RecentlyPlayedMusicState.Success(it.data.items.createTrackDetailsFromItems())
                    }
                }
            }
            fetchAllPlaylists()
        }   
    }

    fun setMusicHeader(header: MusicHeader) {
        _musicHeader.value = header
    }

/*    fun setArtistName(name: String?) {
        if (name != null) {
            _artistName.value = name
        }
    }*/

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

    fun trackSelectAction(action: TrackSelectAction) {
        when(action) {
            is TrackSelectAction.DetailsSelect -> println("MainViewModel ***** DetailsSelect")
            is TrackSelectAction.PlaylistTrackSelect -> {

            }
            is TrackSelectAction.RecentlyPlayedTrackSelect -> {

            }
            is TrackSelectAction.TrackSelect -> {
                println("MainViewModel ***** TRACK")
            }
            is TrackSelectAction.ViewMoreSelect -> {
                println("MainViewModel ***** VIEW MORE")
            }
        }
    }

    fun setPlayList(tracks: List<TrackDetails>) {
        _playlistTracks.value = tracks
    }

    fun isPlaying() {

    }
}
