package com.mccarty.ritmo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.ui.MainComposeScreen
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.utils.positionProduct
import com.mccarty.ritmo.utils.quotientOf
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.viewmodel.TrackSelectAction
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var accessCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        PlayerControls(onSlide = this@MainActivity::playerControlAction)
                    }
                }) { padding ->

                MainComposeScreen(
                    mainViewModel = mainViewModel,
                    padding = padding,
                    viewMore = getString(R.string.sheets_view_more),
                    mediaEvents = object : MediaEvents {
                        override fun trackSelectionAction(
                            trackSelectAction: TrackSelectAction,
                            isPaused: State<Boolean>,
                        ) {
                            trackSelection(trackSelectAction, isPaused)
                        }
                    }
                )
            }
        }

        if (savedInstanceState == null) {
            val request = getAuthenticationRequest()
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
        }

        fetchData()
    }

    override fun onStart() {
        super.onStart()
        SpotifyAppRemote.connect(this, ConnectionParams.Builder(CLIENT_ID).apply {
            setRedirectUri(REDIRECT_URI)
            showAuthView(true)
        }.build(), object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                connect()
            }

            override fun onFailure(throwable: Throwable) {
                // TODO: show an error message
                println("SpotifyBroadcastReceiver ***** ${throwable.message}")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disconnect()
        mainViewModel.cancelJob()
    }

    private fun connect() {
        spotifyAppRemote?.let {
            it.playerApi.playerState
                .setResultCallback { playerState ->
                    mainViewModel.setMusicHeader(MusicHeader().apply {
                        imageUrl = StringBuilder().apply {
                            append(IMAGE_URL)
                            append(playerState.track.imageUri.toString().drop(22).dropLast(2))
                        }.toString()
                        artistName = playerState.track.artist.name ?: getString(R.string.artist_name)
                        albumName = playerState.track.album.name ?: getString(R.string.album_name)
                        songName = playerState.track.name ?: getString(R.string.track_name)
                    })
                    mainViewModel.setTrackUri(playerState.track.uri)
                    mainViewModel.isPaused(playerState.isPaused)
                    mainViewModel.setSliderPosition(
                        position = playerState.playbackPosition,
                        duration = playerState.track?.duration ?: 0,
                        delay = TICKER_DELAY,
                    )

                    mainViewModel.fetchMainMusic()

                    when (mainViewModel.playlistData.value?.name) {
                        PlaylistNames.RECENTLY_PLAYED -> {
                            println("MainActivity ***** connect RECENTLY_PLAYED")
                            mainViewModel.setSliderPosition(
                                position = playerState.playbackPosition,
                                duration = playerState.track?.duration ?: 0,
                                delay = TICKER_DELAY,
                                setPosition = true,
                            )
                        }
                        PlaylistNames.USER_PLAYLIST -> {
                            println("MainActivity ***** USER PLAYLIST")
                        }
                        PlaylistNames.RECOMMENDED_PLAYLIST -> {
                            println("MainActivity ***** connect RECOMMENDED_PLAYLIST")
                            mainViewModel.setSliderPosition(
                                position = playerState.playbackPosition,
                                duration = playerState.track?.duration ?: 0,
                                delay = TICKER_DELAY,
                                setPosition = true,
                            )

                            mainViewModel.setPlaylistData(
                                Playlist(
                                    uri = null,
                                    index = 0,
                                    name = PlaylistNames.RECOMMENDED_PLAYLIST,
                                    tracks = emptyList(),
                                )
                            )
                        } else -> {
                            // Entering app when no playlist data is in memory
                            println("MainActivity ***** connect ELSE ")

                            spotifyAppRemote?.let { remote ->
                                remote.playerApi.playerState.setResultCallback { playerState ->
                                    mainViewModel.setSliderPosition(
                                        position = playerState.playbackPosition,
                                        duration = playerState.track?.duration ?: 0,
                                        delay = TICKER_DELAY,
                                        setPosition = true,
                                    )
                                    mainViewModel.setPlaylistData(
                                        Playlist(
                                            uri = playerState.track?.uri,
                                            index = INITIAL_POSITION,
                                            name = PlaylistNames.RECOMMENDED_PLAYLIST,
                                            tracks = listOf(
                                                MainItem(
                                                    id = null,
                                                    uri = playerState.track?.uri,
                                                    type = null,
                                                    name = playerState.track?.name,
                                                    description = null,
                                                    images = emptyList(),
                                                    tracks = null,
                                                )
                                            ),
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                .setErrorCallback { throwable -> println("MainActivity ***** ERROR ${throwable.message}") } // TODO: handle this
        }
    }

    private fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    private fun getAuthenticationRequest(): AuthorizationRequest? {
        return AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            getRedirectUri().toString()
        )
            .setShowDialog(false)
            .setScopes(
                arrayOf(
                    "user-read-email",
                    "user-read-recently-played",
                    "user-read-playback-state"
                )
            )
            .setCampaign("your-campaign-token")
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val response = AuthorizationClient.getResponse(resultCode, data)
        if (response?.accessToken != null) {
            writeToPreferences(response.accessToken)
            if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
                try {
                    mainViewModel.fetchRecentlyPlayedMusic()
                } catch (ioe: IOException) {
                    // TODO: show some error
                    Log.e(TAG, "${ioe.message}")
                }
            } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
                accessCode = response.code
            }
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.trackUri.collect {
                    it?.let {
                        mainViewModel.fetchRecentlyPlayedMusic()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.trackEnd.collect { trackEnded ->
                    playerControlAction(
                        action = PlayerControlAction.Skip(0),
                        trackEnded = trackEnded,
                    )
                }
            }
        }
    }

    private fun getRedirectUri(): Uri? {
        return Uri.parse(REDIRECT_URI)
    }

    private fun playerControlAction(action: PlayerControlAction, trackEnded: Boolean = false) {
        when (action) {
            PlayerControlAction.Back -> {
                spotifyAppRemote?.let { remote ->
                    remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                        mainViewModel.setLastPlayedTrackData(playerState.track)
                    }

                    mainViewModel.isPaused(false)
                    mainViewModel.setPlaybackPosition(0)
                    remote.playerApi.skipPrevious()
                }
            }

            is PlayerControlAction.Play -> {
                spotifyAppRemote?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        mainViewModel.resumePlayback(
                            position = playerState.playbackPosition.quotientOf(TICKER_DELAY),
                            playerState = playerState,
                            remote = it,
                        )
                    }
                }
            }

            is PlayerControlAction.Seek -> {
                mainViewModel.cancelJob()
                mainViewModel.setPlaybackPosition(action.position.toInt())
                spotifyAppRemote?.playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
            }

            is PlayerControlAction.Skip -> {
                println("MainActivity ***** playerControlAction()")
                when(mainViewModel.playlistData.value?.name) {
                    PlaylistNames.RECOMMENDED_PLAYLIST -> {
                        println("MainActivity ***** playerControlAction() RECOMMENDED_PLAYLIST")
                        mainViewModel.setPlaylistData(
                            Playlist(
                                uri = mainViewModel.recommendedPlaylist[INITIAL_POSITION].track?.uri,
                                index = INITIAL_POSITION,
                                name = PlaylistNames.RECENTLY_PLAYED,
                                tracks = mainViewModel.recommendedPlaylist,
                            )
                        )
                        if (!trackEnded) {
                            mainViewModel.cancelJob()
                        }
                        setupSliderPosition()
                    }

                    PlaylistNames.RECENTLY_PLAYED -> {
                        println("MainActivity ***** playerControlAction() RECENTLY_PLAYED")
                        if (!trackEnded) {
                            mainViewModel.cancelJob()
                        }
                        setupSliderPosition(INCREMENT_INDEX)
                    }
                    PlaylistNames.USER_PLAYLIST -> { /* TODO */}
                    else -> { setupSliderPosition(INCREMENT_INDEX) }
                }
            }

            is PlayerControlAction.PlayWithUri -> {
                spotifyAppRemote?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        mainViewModel.isPaused(playerState.isPaused)
                        if (playerState.isPaused) {
                            spotifyAppRemote?.playerApi?.play(action.uri)
                        } else {
                            spotifyAppRemote?.playerApi?.pause()
                        }
                    }
                }
            }

            PlayerControlAction.ResetToStart -> {
                mainViewModel.fetchCurrentlyPlayingTrack()
            }
        }
    }

    private fun trackSelection(
        action: TrackSelectAction,
        isPaused: State<Boolean>,
    ) {
        when(action) {
            is TrackSelectAction.TrackSelect -> {
                if (isPaused.value) {
                    spotifyAppRemote?.let { remote ->
                        mainViewModel.isPaused(false)
                        mainViewModel.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        mainViewModel.handlePlayerActions(remote, action)
                    }
                    mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                    mainViewModel.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = action.tracks,
                        )
                    )
                } else {
                    spotifyAppRemote?.let { remote ->
                        mainViewModel.isPaused(false)
                        mainViewModel.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        mainViewModel.handlePlayerActions(remote, action)
                    }
                    mainViewModel.cancelJob()
                    mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                    mainViewModel.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = action.tracks,
                        )
                    )
                }
            }
            is TrackSelectAction.PlayTrackWithUri -> {
                if (isPaused.value) {
                    spotifyAppRemote?.playerApi?.play(action.playTrackWithUri)
                    mainViewModel.isPaused(false)
                } else {
                    spotifyAppRemote?.playerApi?.pause()
                    mainViewModel.isPaused(true)
                }
            }
        }
    }
    private fun writeToPreferences(token: String) {
        val pref = this@MainActivity.getSharedPreferences(SPOTIFY_TOKEN, Context.MODE_PRIVATE)
        with (pref.edit()) {
            putString(SPOTIFY_TOKEN, token)
            apply()
        }
    }

    private fun setupSliderPosition(index: Int = INITIAL_POSITION) {
        spotifyAppRemote?.let { remote ->
            when(mainViewModel.playlistData.value?.name) {
                PlaylistNames.RECENTLY_PLAYED -> {
                    if ((mainViewModel.playlistData.value?.index ?: 0) == (mainViewModel.playlistData.value?.tracks?.lastIndex)) {
                        println("MainActivity ***** WHAT IS IT IF")
                        remote.playerApi.play(null)
                        mainViewModel.setPlaylistData(null)
                    } else {
                        println("MainActivity ***** WHAT IS IT ELSE") // TODO: auto play
                        val newIndex =  mainViewModel.newIndex(index)
                        val theUri = mainViewModel.getUri(newIndex)

                        // println("MainActivity ***** TRACK NAME ${model.playlistData.value?.tracks!![newIndex].track?.name}")
                        // println("MainActivity ***** TRACK DURATION ${model.playlistData.value?.tracks!![newIndex]?.track?.duration_ms?.quotientOf(TICKER_DELAY)}")

                        mainViewModel.setPlaylistData(
                            mainViewModel.playlistData.value?.copy(
                                uri = theUri,
                                index =  newIndex,
                            )
                        )
                        mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                        mainViewModel.playbackDuration(mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.quotientOf(TICKER_DELAY) ?: 0)
                        remote.playerApi.play(theUri)
                    }
                }
                PlaylistNames.USER_PLAYLIST -> { }
                PlaylistNames.RECOMMENDED_PLAYLIST -> {
                    mainViewModel.setPlaylistData(
                        Playlist(
                            uri = mainViewModel.recommendedPlaylist[INITIAL_POSITION].track?.uri,
                            index = INITIAL_POSITION,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = mainViewModel.recommendedPlaylist,
                        )
                    )
                }
                else -> {
                    setupSliderPosition()
                }
            }
        }
    }

    companion object {
        const val INDEX_KEY = "index"
        const val PLAYLIST_NAME_KEY = "playlist_name/"
        const val MAIN_SCREEN_KEY = "main_screen"
        const val PLAYLIST_SCREEN_KEY = "playlist_screen/"
        const val SONG_DETAILS_KEY = "song_details/"

        const val SPOTIFY_TOKEN = "SPOTIFY_TOKEN"
        const val AUTH_TOKEN_REQUEST_CODE = 0x10
        const val AUTH_CODE_REQUEST_CODE = 0x11
        const  val  REDIRECT_URI = "com.mccarty.ritmo://auth"
        const val IMAGE_URL = "https://i.scdn.co/image/"
        const val TICKER_DELAY = 1_000L
        const val INCREMENT_INDEX = 1
        const val API_SEED_TRACKS = 2
        const val API_SEED_ARTISTS = 3
        const val  INITIAL_POSITION = 0

        val TAG = MainActivity::class.qualifiedName
    }

    interface MediaEvents {
        fun trackSelectionAction(trackSelectAction: TrackSelectAction, isPaused: State<Boolean>)
    }
}
