package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.MainActivity.Companion.API_SEED_ARTISTS
import com.mccarty.ritmo.MainActivity.Companion.API_SEED_TRACKS
import com.mccarty.ritmo.MainActivity.Companion.INITIAL_POSITION
import com.mccarty.ritmo.MainActivity.Companion.TICKER_DELAY
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.MediaDetails
import com.mccarty.ritmo.domain.MediaTicker
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
import com.mccarty.ritmo.utils.createTrackDetailsFromItemsRecommended
import com.mccarty.ritmo.utils.createTrackDetailsFromPlayListItems
import com.mccarty.ritmo.utils.quotientOf
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    @Inject
    lateinit var mediaTickerFactory: MediaTicker.MediaTickerFactory

    sealed class RecentlyPlayedMusicState {
        data class Success(val trackDetails: List<MainItem> = emptyList()) :
            RecentlyPlayedMusicState()
    }

    sealed class MainMusicState {
        data object Pending : MainMusicState()
        data class Success(
            val trackDetails: List<MainItem> = emptyList(),
            val playLists: List<PlaylistData.Item> = emptyList(),
        ) : MainMusicState()

        data object Error : MainMusicState()
    }

    sealed class Recently {
        data object Pending : Recently()
        data class Success(val success: String) : Recently()
        data object Error : Recently()
    }

    sealed class AllPlaylistsState {
        data class Pending(val pending: Boolean) : AllPlaylistsState()
        data class Success(val playLists: List<PlaylistData.Item>) : AllPlaylistsState()
        data object Error : AllPlaylistsState()
    }

    sealed class PlaylistState {
        data class Pending(val pending: Boolean) : PlaylistState()

        data class Success(val trackDetails: List<TrackDetails>) : PlaylistState()
        data object Error : PlaylistState()
    }

    sealed class LastPlayedSongState {
        data class Pending(val pending: Boolean) : LastPlayedSongState()
        data class Success<T : AlbumXX>(val data: T) : LastPlayedSongState()
        data object Error : LastPlayedSongState()
    }

    sealed class CurrentlyPayingTrackState {
        data class Pending(val pending: Boolean) : CurrentlyPayingTrackState()
        data class Success<T : CurrentlyPlayingTrack>(val data: T) : CurrentlyPayingTrackState()
        data object Error : CurrentlyPayingTrackState()
    }

    /** Recently played items top of main screen */
    sealed class MainItemsState {
        data class Pending(val pending: Boolean) : MainItemsState()
        data class Success(val mainItems: Map<String, List<MainItem>>) : MainItemsState()
        data class Error(val error: Boolean) : MainItemsState()
    }

    private var _recentlyPlayed = MutableStateFlow<List<TrackV2Item>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackV2Item>> = _recentlyPlayed

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(
        RecentlyPlayedMusicState.Success(emptyList())
    )
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _playLists = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playLists: StateFlow<PlaylistState> = _playLists

    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending(true))
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending(true))
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _currentlyPlayingTrack = MutableStateFlow<CurrentlyPayingTrackState>(CurrentlyPayingTrackState.Pending(true))
    val currentlyPayingTrackState: StateFlow<CurrentlyPayingTrackState> = _currentlyPlayingTrack

    private var _playlistTracks = MutableStateFlow<List<MainItem>>(emptyList())
    val playlistTracks: StateFlow<List<MainItem>> = _playlistTracks

    private var _recommendedTracks = MutableStateFlow<List<MainItem>>(emptyList())
    val recommendedTracks: StateFlow<List<MainItem>> = _recommendedTracks

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
    val isPaused: StateFlow<Boolean> = _isPaused

    private var _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private var _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private var _mainItems = MutableStateFlow<MainItemsState>(MainItemsState.Pending(true))
    val mainItems: StateFlow<MainItemsState> = _mainItems

    private var _trackEnd = MutableSharedFlow<Boolean>(1)
    val trackEnd = _trackEnd

    private var _lastPlayedTrackData = MutableSharedFlow<ControlTrackData?>(replay = 1)
    val lastPlayedTrackData = _lastPlayedTrackData

    private val _playlistData = MutableStateFlow<Playlist?>(null)
    val playlistData = _playlistData

    private var _recommendedPlaylist = mutableListOf<MainItem>() // TODO: change name
        val recommendedPlaylist: List<MainItem>
        get() = _recommendedPlaylist

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
                when (it) {
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
                _recentlyPlayedMusic.update { RecentlyPlayedMusicState.Success(emptyList()) }
            }.collect { items ->
                when (items) {
                    is NetworkRequest.Error -> {
                        _recentlyPlayedMusic.update {
                            RecentlyPlayedMusicState.Success(emptyList())
                        }
                    }

                    is NetworkRequest.Success -> {
                        _recentlyPlayedMusic.update { _ ->
                            RecentlyPlayedMusicState.Success(
                                items.data.items.createTrackDetailsFromItems()
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
                    is NetworkRequest.Error -> {
                        _mainItems.value = MainItemsState.Error(true)
                    }

                    is NetworkRequest.Success -> {
                        val trackItems =
                            it.data.items.distinctBy { track -> track.track?.id }.map { track ->
                                TrackItem(
                                    context = track.context,
                                    played_at = track.played_at,
                                    track = track.track,
                                )
                            }
                        mainItems.addAll(trackItems)
                        _recommendedPlaylist.clear()
                        _recommendedPlaylist.addAll(trackItems)
                    }
                }
            }
            repository.fetchPlayLists().collect {
                when (it) {
                    is NetworkRequest.Error -> {
                        MainItemsState.Error(true)
                    }

                    is NetworkRequest.Success -> {
                        val listItems = it.data.items.map { item ->
                            ListItem(
                                collaborative = item.collaborative,
                                description = item.description,
                                external_urls = item.external_urls,
                                href = item.href,
                                id = item.id,
                                images = item.images, // TODO: null image fix
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
            _mainItems.value = MainItemsState.Success(mainItems.groupBy { it.type ?: "" }) // TODO: null ok
        }
    }

    fun fetchCurrentlyPlayingTrack() {
        viewModelScope.launch {
            repository.fetchCurrentlyPlayingTrack().collect { it ->
                when (it) {
                    is NetworkRequest.Error -> {
                        "handle error "
                    }

                    is NetworkRequest.Success -> {
                        _currentlyPlayingTrack.value = CurrentlyPayingTrackState.Success(it.data)
                    }
                }
            }
        }
    }

    fun fetchRecommendedPlaylist(mainItems: List<MainItem>) {
        val ids = mutableListOf<String>()
        val artists = mutableListOf<List<String>>()

        mainItems.map {
            ids.add(it.track?.id.toString())
            it.track?.artists?.map { artist -> artist.id }?.let { item -> artists.add(item) }
        }
        val trackIds = ids.take(API_SEED_TRACKS).joinToString().filter { !it.isWhitespace() }
        val artistIds = artists.flatten().take(API_SEED_ARTISTS).joinToString().filter { !it.isWhitespace() }

        viewModelScope.launch {
            repository.fetchRecommendedPlaylists(trackIds, artistIds).collect { items ->
                when(items) {
                    is NetworkRequest.Error -> {  println("MainViewModel ***** DATA ITEMS ERROR ${items.toString()}") }
                    is NetworkRequest.Success -> {
                        _recommendedPlaylist.addAll(items.data.tracks.createTrackDetailsFromItemsRecommended())
                        println("MainViewModel ***** THE TRACKS ${items.data.tracks.createTrackDetailsFromItemsRecommended().toString()}}")
                    }
                }
            }
        }
    }

    private var job: Job? = null
    fun setSliderPosition() {
        job = viewModelScope.launch {
            println("MainViewModel ***** DURATION ${playbackDuration.value}")
            job?.cancelAndJoin()
            mediaTickerFactory.create(
                playbackPosition.value,
                playbackDuration.value,
                TICKER_DELAY,
            ).mediaTicker().collect { position ->
                println("MainViewModel ***** $position")
                _playbackPosition.update { position }
                if (position == playbackDuration.value) {
                    _playbackPosition.update { 0L }
                    _trackEnd.tryEmit(true)
                }
            }
        }
    }

    fun setSliderPosition(
        position: Long,
        duration: Long? = 0,
        delay: Long,
        setPosition: Boolean = false,
    ) {
        playbackPosition(position.quotientOf(delay))
        playbackDuration(duration?.quotientOf(delay))

        if (setPosition) {
            setSliderPosition()
        }
    }

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
        viewModelScope.launch {
            _isPaused.emit(isPaused)
        }
    }

    fun playbackDuration(duration: Long?) {
        _playbackDuration.update {
            duration ?: 0
        }
    }

    fun playbackDurationWithIndex(newIndex: Int) {
        _playbackDuration.update {
            playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.quotientOf(TICKER_DELAY) ?: 0
        }
    }

    fun <T : Number> playbackPosition(position: T) {
        _playbackPosition.value = position.toLong()
    }

    fun handlePlayerActions(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect) {
        remoteService.onTrackSelected(remote, action)
    }

    fun setLastPlayedTrackData(track: com.spotify.protocol.types.Track) {
        viewModelScope.launch {
            _lastPlayedTrackData.emit(ControlTrackData(duration = track.duration))
        }
    }

    fun cancelJobIfRunning() {
        viewModelScope.launch {
            job?.cancelAndJoin()
        }
    }

    fun setPlaylistData(playlist: Playlist?) {
        _playlistData.update { playlist }
    }

    fun updatedIndex() = playlistData.value?.index?.plus(0) ?: INITIAL_POSITION

    fun updatedUri(index: Int) = playlistData.value?.tracks?.get(index)?.track?.uri.toString()

    fun updatedUriRecommended(index: Int) = recommendedPlaylist[index].track?.uri.toString()

    /** If index less than zero, then this is the first time setting up data */
    fun firstTimePlayingRecommended(): Boolean = (playlistData.value?.index ?: INITIAL_POSITION) < INITIAL_POSITION

}
data class ControlTrackData(
    var duration: Long
)

data class Playlist(
    val uri: String?,
    var index: Int,
    val name: PlaylistNames?,
    val tracks: List<MainItem>?,
)

enum class PlaylistNames {
    RECENTLY_PLAYED,
    USER_PLAYLIST,
    RECOMMENDED_PLAYLIST,
}