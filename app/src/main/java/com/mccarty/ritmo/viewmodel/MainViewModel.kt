package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.MediaDetails
import com.mccarty.ritmo.domain.RemoteService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.model.TrackV2Item
import com.mccarty.ritmo.model.payload.ListItem
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.TrackItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.createTrackDetailsFromItems
import com.mccarty.ritmo.utils.createTrackDetailsFromPlayListItems
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val remoteService: RemoteService,
    private val details: MediaDetails,
    ) : ViewModel() {

    sealed class RecentlyPlayedMusicState {
        data class Success(val trackDetails: List<MainItem> = emptyList()): RecentlyPlayedMusicState()
    }

    sealed class MainMusicState {
        data object Pending: MainMusicState()
        data class Success(
            val trackDetails: List<MainItem> = emptyList(),
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

    sealed class MainItemsState {
        data class Pending(val pending: Boolean): MainItemsState()
        data class Success(val mainItems: Map<String, List<MainItem>>): MainItemsState()
        data class  Error(val error: Boolean): MainItemsState()
    }

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(
        RecentlyPlayedMusicState.Success(emptyList())
    )
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic.asStateFlow()

    private var _playLists = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playLists: StateFlow<PlaylistState> = _playLists

    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending(true))
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _playlistTracks = MutableStateFlow<List<MainItem>>(emptyList())
    val playlistTracks: StateFlow<List<MainItem>> = _playlistTracks

    private var _mediaDetails = MutableStateFlow<List<Details>>(emptyList())
    val mediaDetails: StateFlow<List<Details>> = _mediaDetails

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(
        LastPlayedSongState.Pending(
            true
        )
    )
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong

    private var _musicHeader = MutableStateFlow(MusicHeader())
    val musicHeader: StateFlow<MusicHeader> = _musicHeader

    private var _trackUri = MutableStateFlow<String?>(null)
    val trackUri: StateFlow<String?> = _trackUri

    private var _isPaused = MutableStateFlow(true)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var _playbackDuration = MutableStateFlow<Number>(0)
    val playbackDuration: StateFlow<Number> = _playbackDuration

    private var _playbackPosition = MutableStateFlow(0f)
    val playbackPosition: StateFlow<Float> = _playbackPosition

    private var _mainItems = MutableStateFlow<MainItemsState>(MainItemsState.Pending(true))
    val mainItems: StateFlow<MainItemsState> = _mainItems

    private var _isScreenVisible = MutableStateFlow<Boolean>(false)
    val isScreenVisible = _isScreenVisible.asStateFlow()

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
        _playLists.value = PlaylistState.Pending(true)
        viewModelScope.launch {
            repository.fetchPlayList(playlistId).collect {
                when(it) {
                    is NetworkRequest.Error -> {
                        println("MainViewModel ***** NET ERROR ${it.toString()}")
                        // TODO: handle error
                    }
                    is NetworkRequest.Success -> {
                        _playLists.value =
                            PlaylistState.Success(it.data.items.createTrackDetailsFromPlayListItems())
                    }
                }
            }
        }
    }

    fun fetchRecentlyPlayedMusic() {
        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(emptyList())
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> {
                        _recentlyPlayedMusic.update {
                            RecentlyPlayedMusicState.Success(emptyList())
                        }
                    }

                    is NetworkRequest.Success -> {
                        _recentlyPlayedMusic.update { _ ->
                            RecentlyPlayedMusicState.Success(
                                it.data.items.createTrackDetailsFromItems()
                            )
                        }
                    }
                }
            }
            fetchAllPlaylists()
        }
    }

    fun fetchMainMusic() {
        val mainItems = mutableListOf<MainItem>()
        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(emptyList())
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> { _mainItems.value = MainItemsState.Error(true) }
                    is NetworkRequest.Success -> {
                        val trackItems = it.data.items.distinctBy { track -> track.track?.id }.map { track ->
                            TrackItem(
                                context = track.context,
                                played_at = track.played_at,
                                track = track.track,
                            )
                        }
                        mainItems.addAll(trackItems)
                    }
                }
            }
            repository.fetchPlayLists().collect {
                when (it) {
                    is NetworkRequest.Error -> { MainItemsState.Error(true) }
                    is NetworkRequest.Success -> {
                        val listItems = it.data.items.map { item ->
                            ListItem(
                                collaborative = item.collaborative,
                                description = item.description,
                                external_urls = item.external_urls,
                                href = item.href,
                                id = item.id,
                                images = item.images,
                                name = item.name,
                                owner = item.owner,
                                public = item.public,
                                snapshot_id = item.snapshot_id,
                                tracks = item.tracks,
                                type = item.type,
                                uri = item.uri,
                            )
                        }
                        mainItems.addAll(listItems)
                    }
                }
            }
            _mainItems.value = MainItemsState.Success( mainItems.groupBy { it.type } )
        }
    }

/*    suspend fun fetchPlaybackState() {
        viewModelScope.launch {
            while (!isPaused.value) {
                repository.fetchPlaybackState()
                    .collect { request ->
                        when (request) {
                        is NetworkRequest.Error -> {
                            _isPaused.update { it }
                        }

                        is NetworkRequest.Success -> {
                            _playbackPosition.update { request.data.progress_ms.toFloat() }
                        }
                    }
                }
            }
        }
    }*/

    fun setMusicHeader(header: MusicHeader) {
        _musicHeader.value = header
    }

    fun setTrackUri(trackUri: String?) {
        _trackUri.value = trackUri
    }

    fun setPlayList(tracks: List<Any>) {
        _mediaDetails.value = details.mediaDetails(tracks)
    }

    fun isPaused(isPaused: Boolean) {
        _isPaused.value = isPaused
    }

    fun<T: Number> playbackDuration(duration: T) {
        _playbackDuration.update { duration }
    }

    fun<T: Number> playbackPosition(position: T) {
        _playbackPosition.value = position.toFloat()
    }

    fun handlePlayerActions(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect) {
        remoteService.onTrackSelected(remote, action)
    }

    fun <T : Number> getSliderPosition(position: T) {
        viewModelScope.launch {
            var increment = position.toFloat()
            increment++
            _playbackPosition.update { increment }
        }
    }

    fun setIsScreenVisible(isScreenVisible: Boolean) {
        _isScreenVisible.update { isScreenVisible }
    }
}
