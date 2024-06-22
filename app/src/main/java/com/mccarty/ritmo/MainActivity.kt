package com.mccarty.ritmo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.model.MusicHeader
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

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
                    mainViewModel = model,
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
        model.cancelJobIfRunning()
    }

    private fun connect() {
        spotifyAppRemote?.let {
            it.playerApi.playerState
                .setResultCallback { playerState ->
                    model.setMusicHeader(MusicHeader().apply {
                        imageUrl = StringBuilder().apply {
                            append(IMAGE_URL)
                            append(playerState.track.imageUri.toString().drop(22).dropLast(2))
                        }.toString()
                        artistName = playerState.track.artist.name ?: getString(R.string.artist_name)
                        albumName = playerState.track.album.name ?: getString(R.string.album_name)
                        songName = playerState.track.name ?: getString(R.string.track_name)
                    })
                    model.setTrackUri(playerState.track.uri)
                    model.isPaused(playerState.isPaused)
                    model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                    model.playbackPosition(playerState.playbackPosition.quotientOf(TICKER_DELAY))
                    model.fetchMainMusic()

                    if (!playerState.isPaused) {
                        model.setSliderPosition()
                        model.fetchCurrentlyPlayingTrack()
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
                    model.fetchRecentlyPlayedMusic() // TODO: called above
                } catch (ioe: IOException) {
                    // TODO: show some error
                    Log.e(TAG, "${ioe.message}")
                }
            } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
                accessCode = response.code // TODO: where is this used
            }
        }
    }

    private fun fetchData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.trackUri.collect {
                    it?.let {
                        model.fetchRecentlyPlayedMusic() // TODO: called twice
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.isPaused.collect {
                    if (!it) {
                        model.fetchCurrentlyPlayingTrack()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.trackEnd.collect { trackEnded ->
                    if (trackEnded) {
                        setupSliderPosition()
                    }
                }
            }
        }
    }

    private fun getRedirectUri(): Uri? {
        return Uri.parse(REDIRECT_URI)
    }

    private fun playerControlAction(action: PlayerControlAction) {
        when (action) {
            PlayerControlAction.Back -> {
                spotifyAppRemote?.let { remote ->
                    remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                        model.setLastPlayedTrackData(playerState.track)
                    }
                    model.isPaused(false)
                    model.cancelJobIfRunning()
                    model.playbackPosition(0)
                    remote.playerApi.skipPrevious()
                    model.setSliderPosition()
                }
            }

            is PlayerControlAction.Play -> {
                spotifyAppRemote?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        if (playerState.isPaused) {
                            model.isPaused(false)
                            spotifyAppRemote?.playerApi?.resume()
                            model.playbackPosition(action.pausedPosition)
                            model.setSliderPosition()
                        } else {
                            model.isPaused(true)
                            model.playbackPosition(playerState.playbackPosition.quotientOf(TICKER_DELAY))
                            model.cancelJobIfRunning()
                            spotifyAppRemote?.playerApi?.pause()
                        }
                    }
                }
            }

            is PlayerControlAction.Seek -> {
                model.cancelJobIfRunning()
                model.playbackPosition(action.position.toLong())
                spotifyAppRemote?.playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
                model.setSliderPosition()
            }

            is PlayerControlAction.Skip -> {
                setupSliderPosition()
            }

            is PlayerControlAction.PlayWithUri -> {
                spotifyAppRemote?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        model.isPaused(playerState.isPaused)
                        if (playerState.isPaused) {
                            spotifyAppRemote?.playerApi?.play(action.uri)
                        } else {
                            spotifyAppRemote?.playerApi?.pause()
                        }
                    }
                }
            }

            PlayerControlAction.ResetToStart -> {
                model.fetchCurrentlyPlayingTrack()
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
                        model.isPaused(false) // TODO: main select
                        model.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        model.handlePlayerActions(remote, action)
                    }
                    model.playbackPosition(0)
                    model.setSliderPosition() // TODO: when not paying yet
                    model.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = action.tracks,
                        )
                    )
                } else {
                    spotifyAppRemote?.let { remote ->
                        model.isPaused(false) // TODO: main select
                        model.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        model.handlePlayerActions(remote, action)
                    }
                    model.playbackPosition(0)
                    model.setSliderPosition()
                    model.setPlaylistData(
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
                    model.isPaused(false)
                } else {
                    spotifyAppRemote?.playerApi?.pause()
                    model.isPaused(true)
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

    private fun setupSliderPosition() {
        spotifyAppRemote?.let { remote ->
            remote.playerApi.playerState.setResultCallback { playerState ->
                when (model.playlistData.value?.name) {
                    PlaylistNames.RECENTLY_PLAYED -> {
                        if ((model.playlistData.value?.index
                                ?: 0) == (model.playlistData.value?.tracks?.lastIndex)
                        ) {
                            remote.playerApi.play(null)
                            model.setPlaylistData(null)
                            println("MainActivity ***** SKIP 1")
                            // TODO: set recommneded playlist
                        } else {
                            val newIndex = model.playlistData.value?.index?.plus(1) ?: 0
                            val theUri =
                                model.playlistData.value?.tracks?.get(newIndex)?.track?.uri.toString()

                            model.setPlaylistData(
                                model.playlistData.value?.copy(
                                    uri = theUri,
                                    index = newIndex
                                )
                            )
                            model.playbackPosition(0)
                            model.playbackDurationWithIndex(newIndex)
                            model.playbackDuration(
                                model.playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.quotientOf(
                                    TICKER_DELAY
                                )
                            )
                            model.setSliderPosition()
                            model.isPaused(false)

                            remote.playerApi.play(theUri)

                            println("MainActivity ***** SKIP 2")
                        }
                    }

                    PlaylistNames.RECOMMENDED_PLAYLIST -> {
                        println("MainActivity ***** RECOMMENDED PLAYLIST")
                        model.fetchRecommendedPlaylist()
                    }

                    PlaylistNames.USER_PLAYLIST -> {
                        println("MainActivity ***** SKIP USER LIST")
                    }

                    else -> {
                        println("MainActivity ***** ELSE")
                       /* model.playbackPosition(0)
                        model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                        model.setSliderPosition()
                        remote.playerApi.skipNext()
                        model.isPaused(false)

                        println("MainActivity ***** SKIP DURATION ${playerState.track.duration.quotientOf(TICKER_DELAY)}")
                        println("MainActivity ***** SKIP ARTIST ${playerState.track.artist}")*/

                        // model.fetchRecommendedPlaylist()
                    }
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
        const val API_SEED_TRACKS = 2
        const val API_SEED_ARTISTS = 3

        val TAG = MainActivity::class.qualifiedName
    }

    interface MediaEvents {
        fun trackSelectionAction(trackSelectAction: TrackSelectAction, isPaused: State<Boolean>)
    }
}
