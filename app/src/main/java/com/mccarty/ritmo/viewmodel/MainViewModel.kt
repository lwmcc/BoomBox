package com.mccarty.ritmo

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.domain.RemoteService
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.model.TrackV2Item
import com.mccarty.ritmo.model.payload.ArtistX
import com.mccarty.ritmo.model.payload.Image
import com.mccarty.ritmo.model.payload.ListItem
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.model.payload.TrackItem
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.createTrackDetailsFromItems
import com.mccarty.ritmo.utils.createTrackDetailsFromPlayListItems
import com.mccarty.ritmo.viewmodel.TrackSelectAction
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val remoteService: RemoteService,
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

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(RecentlyPlayedMusicState.Success(emptyList()))
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _playLists = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playLists: StateFlow<PlaylistState> = _playLists

    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending(true))
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _playlistTracks = MutableStateFlow<List<MainItem>>(emptyList())
    val playlistTracks: StateFlow<List<MainItem>> = _playlistTracks

    private var _mediaDetails = MutableStateFlow<List<MediaDetails>>(emptyList())
    val mediaDetails: StateFlow<List<MediaDetails>> = _mediaDetails

    private var _lastPlayedSong = MutableStateFlow<LastPlayedSongState>(LastPlayedSongState.Pending(true))
    val lastPlayedSong: StateFlow<LastPlayedSongState> = _lastPlayedSong

    private var _musicHeader = MutableStateFlow(MusicHeader())
    val musicHeader: StateFlow<MusicHeader> = _musicHeader

    private var _trackUri = MutableStateFlow<String?>(null)
    val trackUri: StateFlow<String?> = _trackUri

    private var _isPaused = MutableStateFlow(true)
    val isPaused: StateFlow<Boolean> = _isPaused

    private var _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private var _playbackPosition = MutableStateFlow(0f)
    val playbackPosition: StateFlow<Float> = _playbackPosition

    private var _mainItems = MutableStateFlow<Map<String, List<MainItem>>>(emptyMap())
    val mainItems: StateFlow<Map<String, List<MainItem>>> = _mainItems

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
        viewModelScope.launch {
            _playLists.value = PlaylistState.Pending(true)
            repository.fetchPlayList(playlistId).collect {
                when(it) {
                    is NetworkRequest.Error -> {
                        println("MainViewModel ***** NET ERROR ${it.toString()}")
                        // TODO: handle error
                    }
                    is NetworkRequest.Success -> {
                        println("MainViewModel ***** NET SUCCESS ${it.toString()}")
                        _playLists.value = PlaylistState.Success(it.data.items.createTrackDetailsFromPlayListItems())
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

    fun fetchMainMusic() {

        val mainItems = mutableStateListOf<MainItem>()

        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(emptyList())
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> { }
                    is NetworkRequest.Success -> {
                        val trackItems = it.data.items.map { it ->
                            TrackItem(
                                context = it.context,
                                played_at = it.played_at,
                                track = it.track,
                            )
                        }
                        mainItems.addAll(trackItems)
                    }
                }
            }
            repository.fetchPlayLists().collect {
                when (it) {
                    is NetworkRequest.Error -> { }
                    is NetworkRequest.Success -> {
                        val listItems = it.data.items.map { it ->
                            ListItem(
                                collaborative = it.collaborative,
                                description = it.description,
                                external_urls = it.external_urls,
                                href = it.href,
                                id = it.id,
                                images = it.images,
                                name = it.name,
                                owner = it.owner,
                                public = it.public,
                                snapshot_id = it.snapshot_id,
                                tracks = it.tracks,
                                type = it.type,
                                uri = it.uri,
                            )
                        }
                        mainItems.addAll(listItems)
                    }
                }
            }
            _mainItems.value = mainItems.groupBy { it.type }
        }
    }

    suspend fun fetchPlaybackState() {
        repository.fetchPlaybackState().collect {
            when (it) {
                is NetworkRequest.Error -> println("ERROR ***** ${it}")
                is NetworkRequest.Success -> { _playbackPosition.value = it.data.progress_ms.toFloat() }
            }
        }
    }

    fun setMusicHeader(header: MusicHeader) {
        _musicHeader.value = header
    }

    fun setTrackUri(trackUri: String?) {
        _trackUri.value = trackUri
    }

    fun setPlayList(tracks: List<Any>) {

        when (tracks.firstOrNull()) {
            is TrackDetails -> {
                _mediaDetails.value = tracks.map {
                    it as TrackDetails
                    MediaDetails(
                        albumName = it.albumName,
                        trackName = it.trackName,
                        explicit = it.explicit,
                        artists = it.artists,
                        images = it.images,
                        trackId = it.id,
                        uri = it.uri,
                        type = it.type,
                    )
                }
            }

            is MainItem -> {
                _mediaDetails.value = tracks.map {
                    it as MainItem
                    MediaDetails(
                        albumName = it.track?.album?.name,
                        trackName = it.track?.name,
                        explicit = it.track?.explicit ?: false,
                        artists = it.track?.artists,
                        images = it.track?.album?.images,
                        trackId = it.track?.id,
                        uri = it.track?.uri,
                        type = it.track?.type,
                    )
                }
            }

            else -> {
                _mediaDetails.value = emptyList()
            }
        }
    }

    fun isPaused(isPaused: Boolean) {
        _isPaused.value = isPaused
    }

    fun playbackDuration(duration: Long) {
        _playbackDuration.value = duration
    }

    fun playbackPosition(position: Float) {
        _playbackPosition.value = position
    }

    fun handlePlayerActions(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect) {
        remoteService.onTrackSelected(remote, action)
    }
}

data class MediaDetails(
    val albumName: String?,
    val trackName: String?,
    val explicit: Boolean = false,
    val artists: List<ArtistX>? = emptyList(),
    val images: List<Image>? = emptyList(),
    val trackId: String?,
    val uri: String?,
    val type: String?,
)
