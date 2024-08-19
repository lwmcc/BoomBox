package com.mccarty.ritmo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.networkrequest.network.NetworkRequest
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainActivity.Companion.API_SEED_ARTISTS
import com.mccarty.ritmo.MainActivity.Companion.API_SEED_TRACKS
import com.mccarty.ritmo.MainActivity.Companion.INITIAL_POSITION
import com.mccarty.ritmo.MainActivity.Companion.TICKER_DELAY
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.MediaDetails
import com.mccarty.ritmo.domain.RemoteService
import com.mccarty.ritmo.domain.SliderPosition
import com.mccarty.ritmo.domain.Ticker
import com.mccarty.ritmo.domain.model.AlbumXX
import com.mccarty.ritmo.domain.model.CurrentlyPlayingTrack
import com.mccarty.ritmo.domain.model.MusicHeader
import com.mccarty.ritmo.domain.model.TrackDetails
import com.mccarty.ritmo.domain.model.payload.ListItem
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.TrackItem
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.repository.remote.Repository
import com.mccarty.ritmo.utils.createTrackDetailsFromItems
import com.mccarty.ritmo.utils.createTrackDetailsFromItemsRecommended
import com.mccarty.ritmo.utils.createTrackDetailsFromPlayListItems
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
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
import kotlin.time.Duration.Companion.milliseconds
import com.spotify.protocol.types.Track as Track

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val remoteService: RemoteService,
    private val details: MediaDetails,
    private val sliderTicker: Ticker,
) : ViewModel(), SliderPosition {

    sealed class RecentlyPlayedMusicState {
        data class Success(val trackDetails: List<MainItem> = emptyList()) :
            RecentlyPlayedMusicState()
    }

    sealed class AllPlaylistsState {
        data object Pending: AllPlaylistsState()
        data class Success(val playLists: List<PlaylistData.Item>) : AllPlaylistsState()
        data class Error<T>(val message: T) : AllPlaylistsState()
    }

    sealed class PlaylistState {
        data object Pending : PlaylistState()
        data class Success(val trackDetails: List<TrackDetails>) : PlaylistState()
        data class Error<T>(val message: T) : PlaylistState()
    }

    sealed class CurrentlyPayingTrackState {
        data class Pending(val pending: Boolean) : CurrentlyPayingTrackState()
        data class Success(val data: CurrentlyPlayingTrack) : CurrentlyPayingTrackState()
        data class Error<T>(val message: T) : CurrentlyPayingTrackState()
    }

    /** Recently played items top of main screen */
    sealed class  MainItemsState {
        data object Pending : MainItemsState()
        data class Success(val mainItems: Map<String, List<MainItem>>) : MainItemsState()
        data class Error<T>(val message: T) : MainItemsState()
    }

    private var _album = MutableStateFlow(AlbumXX())
    val album: StateFlow<AlbumXX> = _album

    private var _recentlyPlayedMusic = MutableStateFlow<RecentlyPlayedMusicState>(
        RecentlyPlayedMusicState.Success(emptyList())
    )
    val recentlyPlayedMusic: StateFlow<RecentlyPlayedMusicState> = _recentlyPlayedMusic

    private var _playLists = MutableStateFlow<PlaylistState>(PlaylistState.Pending)
    val playLists: StateFlow<PlaylistState> = _playLists

    private var _allPlaylists = MutableStateFlow<AllPlaylistsState>(AllPlaylistsState.Pending)
    val allPlaylists: StateFlow<AllPlaylistsState> = _allPlaylists

    private var _playlist = MutableStateFlow<PlaylistState>(PlaylistState.Pending) // TODO: remove duplicate code
    val playlist: StateFlow<PlaylistState> = _playlist

    private var _currentlyPlayingTrack = MutableStateFlow<CurrentlyPayingTrackState>(CurrentlyPayingTrackState.Pending(true))
    val currentlyPayingTrackState: StateFlow<CurrentlyPayingTrackState> = _currentlyPlayingTrack

    private var _mediaDetails = MutableStateFlow<List<Details>>(emptyList())
    val mediaDetails: StateFlow<List<Details>> = _mediaDetails

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

    private var _mainItems = MutableStateFlow<MainItemsState>(MainItemsState.Pending)
    val mainItems: StateFlow<MainItemsState> = _mainItems

    private var _trackEnd = MutableSharedFlow<Boolean>(1)
    val trackEnd = _trackEnd

    private var _lastPlayedTrackData = MutableSharedFlow<ControlTrackData?>(replay = 1)
    val lastPlayedTrackData = _lastPlayedTrackData

    private val _playlistData = MutableStateFlow<Playlist?>(null)
    val playlistData = _playlistData

    private var _trackData = MutableStateFlow<MainActivity.TrackData?>(null)
    val trackData: StateFlow<MainActivity.TrackData?> = _trackData

    private var _recommendedPlaylist = mutableListOf<MainItem>()
        val recommendedPlaylist: List<MainItem>
        get() = _recommendedPlaylist

    private var _currentPlaylist = mutableListOf<MainItem>()
    val currentPlaylist: List<MainItem>
        get() = _currentPlaylist

    private suspend fun fetchAllPlaylists() {
        repository.fetchPlayLists().collect {
            when (it) {
                is NetworkRequest.Error -> {
                    AllPlaylistsState.Error(it.message)
                }
                is NetworkRequest.Success -> {
                    _allPlaylists.value = AllPlaylistsState.Success(it.data.items)
                }
            }
        }
    }

    fun fetchPlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.fetchUserPlayList(playlistId).collect {
                when (it) {
                    is NetworkRequest.Error -> {
                        PlaylistState.Error(it.message)
                    }

                    is NetworkRequest.Success -> {
                        val playlistItems = it.data.items.createTrackDetailsFromPlayListItems()
                        _playLists.value =
                            PlaylistState.Success(playlistItems)
                        _currentPlaylist = playlistItems.toMutableList()
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
        val recentItems = mutableListOf<MainItem>()
        val playlistItems = mutableListOf<MainItem>()

        viewModelScope.launch {
            repository.fetchRecentlyPlayedItem().catch {
                _recentlyPlayedMusic.value = RecentlyPlayedMusicState.Success(emptyList())
            }.collect {
                when (it) {
                    is NetworkRequest.Error -> {
                        _mainItems.value = MainItemsState.Error(it.message)
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
                        recentItems.addAll(trackItems)
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
                        playlistItems.addAll(listItems)
                    }
                }
            }

            _mainItems.update {
                MainItemsState.Success(buildMap(2) {
                    put("track", recentItems)
                    put("playlist", playlistItems)
                })
            }
        }
    }

    fun fetchCurrentlyPlayingTrack() {
        viewModelScope.launch {
            repository.fetchCurrentlyPlayingTrack().collect {
                when (it) {
                    is NetworkRequest.Error -> {
                        CurrentlyPayingTrackState.Error(it.message)
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
                    is NetworkRequest.Error -> {
                        // TODO: handle error
                    }
                    is NetworkRequest.Success -> {
                        _recommendedPlaylist.addAll(items.data.tracks.createTrackDetailsFromItemsRecommended())
                    }
                }
            }
        }
    }

    private var job: Job? = null
    private fun setSliderPosition() {

        if (job != null) {
            job?.cancel()
            job = null
        }

        job = viewModelScope.launch {
            sliderTicker.getPlaybackPosition(
                position = playbackPosition.value,
                duration = playbackDuration.value,
                delay = TICKER_DELAY,
            ).collect { position ->
                _playbackPosition.update { position }
                if (position == playbackDuration.value) { // TODO: move to reuse
                    _playbackPosition.update { 0L }
                    _trackEnd.tryEmit(true)
                }
            }
        }
    }

    fun setMusicHeader(header: MusicHeader) {
        _musicHeader.value = header
    }

    fun setMusicHeaderUrl(image: String?, vararg names: String) {
        _musicHeader.update {
            MusicHeader().apply {
                imageUrl = image
                artistName = names[0]
                albumName = names[1]
                songName = names[2]
            }
        }
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
            playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.milliseconds?.inWholeSeconds ?: 0
        }
    }

    private inline fun <reified T : Number> playbackPosition(position: T) {
        _playbackPosition.value = position.toLong()
    }

    fun handlePlayerActions(remote: SpotifyAppRemote?, action: TrackSelectAction.TrackSelect) {
        remoteService.onTrackSelected(remote, action)
    }

    fun setLastPlayedTrackData(track: Track) {
        viewModelScope.launch {
            _lastPlayedTrackData.emit(ControlTrackData(duration = track.duration))
        }
    }

    private fun cancelJobIfRunning() {
        viewModelScope.launch {
            job?.cancelAndJoin()
        }
    }

    fun setMainMusicError(message: String) { _mainItems.value = MainItemsState.Error(message) }

    fun checkIfIndexesEqual(): Boolean =  (playlistData.value?.index ?: 0) == (playlistData.value?.tracks?.lastIndex)

    /** If index less than zero, then this is the first time setting up data */
    fun firstTimePlayingRecommended(): Boolean = (playlistData.value?.index ?: INITIAL_POSITION) < INITIAL_POSITION
    override fun resumePlayback(
        position: Long,
        playerState: PlayerState,
        remote: SpotifyAppRemote
    ) {
        if (playerState.isPaused) {
            remote.playerApi.resume()
            setSliderPosition()
        } else {
            remote.playerApi.pause()
            cancelJobIfRunning()
        }

        isPaused(playerState.isPaused)
        playbackPosition(playerState.playbackPosition.milliseconds.inWholeSeconds)
    }

    override fun newIndex(index: Int) = playlistData.value?.index?.plus(index) ?: INITIAL_POSITION
    override fun getUri(index: Int) = playlistData.value?.tracks?.get(index)?.track?.uri.toString()
    override fun cancelJob() = cancelJobIfRunning()

    override fun setSliderPosition(
        position: Long,
        duration: Long,
        delay: Long,
        setPosition: Boolean,
    ) {
        playbackPosition(position.milliseconds.inWholeSeconds)
        playbackDuration(duration.milliseconds.inWholeSeconds)

        if (setPosition) {
            setSliderPosition()
        }
    }

    override fun setPlaylistData(playlist: Playlist?) {
        _playlistData.update { playlist }
    }

    override fun setPlaybackPosition(position: Int) {
        playbackPosition(position)
        setSliderPosition()
    }

    fun setMainItemsError(message: String) {
        _mainItems.value = MainItemsState.Error(message)
    }

    fun recentlyPlayedMusic(): List<MainItem> {
        return when (val music = recentlyPlayedMusic.value) {
            is RecentlyPlayedMusicState.Success -> {
                music.trackDetails
            }
        }
    }

    fun setBackgroundTrackData(trackData: MainActivity.TrackData) = _trackData.update { trackData }
}

data class ControlTrackData(
    var duration: Long
)

data class Playlist(
    val uri: String?,
    var index: Int,
    val name: PlaylistNames?,
    val tracks: List<MainItem>,
)

enum class PlaylistNames {
    RECENTLY_PLAYED,
    USER_PLAYLIST,
    RECOMMENDED_PLAYLIST,
}