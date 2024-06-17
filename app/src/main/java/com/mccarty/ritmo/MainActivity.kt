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
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.ui.BottomSheet
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.ui.screens.StartScreen
import com.mccarty.ritmo.utils.positionProduct
import com.mccarty.ritmo.utils.quotientOf
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.PlayerControlAction
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sheetState = rememberModalBottomSheetState()
            val scope = rememberCoroutineScope()
            var showBottomSheet by remember { mutableStateOf(false) }
            var trackIndex by remember { mutableIntStateOf(0) }
            val navController = rememberNavController()

            val mainItems = model.mainItems.collectAsStateWithLifecycle()
            val music by remember { mutableStateOf(mainItems) }
            val isPaused = model.isPaused.collectAsStateWithLifecycle()

            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        PlayerControls(onSlide = this@MainActivity::playerControlAction)
                    }
                }) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding(),
                            )
                    ) {
                        StartScreen(
                            navController,
                            music = music,
                            onViewMoreClick = { bottomSheet, index ->
                                showBottomSheet = bottomSheet
                                trackIndex = index
                            },
                            onAction = {
                                trackSelectionAction(
                                    action = it,
                                    isPaused = isPaused,
                                )
                            },
                            onPlayPauseClicked = {
                                trackSelectionAction(
                                    action = it,
                                    isPaused = isPaused,
                                )
                            }
                        )
                    }
                }

                BottomSheet(
                    showBottomSheet,
                    sheetState = sheetState,
                    text = getString(R.string.sheets_view_more),
                    onDismiss = {
                        showSheet(
                            scope = scope,
                            sheetState = sheetState,
                        ) {
                            showBottomSheet = it
                        }
                    },
                    onClick = {
                        showSheet(
                            scope = scope,
                            sheetState = sheetState,
                        ) {
                            showBottomSheet = it
                        }
                        navController.navigate("${MainActivity.SONG_DETAILS_KEY}${trackIndex}")
                    },
                )
            }
        }

        if (savedInstanceState == null) {
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.trackUri.collect {
                    it?.let {
                        model.fetchRecentlyPlayedMusic()
                    }
                }
            }
        }
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
                // TODO: log this
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

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(
            CLIENT_ID,
            type,
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
            writeToPreferences(response.accessToken, SPOTIFY_TOKEN)
            if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
                try {
                    model.fetchRecentlyPlayedMusic() // TODO: called above
                } catch (ioe: IOException) {
                    // TODO: show some error
                    Log.e(TAG, "${ioe.message}")
                }
            } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
                accessCode = response.code
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
                    model.playbackPosition(0f)
                    model.cancelJobIfRunning()
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
                model.playbackPosition(action.position)
                spotifyAppRemote?.playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
            }

            PlayerControlAction.Skip -> {
                spotifyAppRemote?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        if (playerState.isPaused) {
                            model.playbackPosition(0f)
                            spotifyAppRemote?.playerApi?.skipNext()
                            model.isPaused(false)
                            model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                            model.setSliderPosition()
                        } else {
                            model.playbackPosition(0f)
                            spotifyAppRemote?.playerApi?.skipNext()
                            model.isPaused(false)
                            model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                            model.cancelJobIfRunning()
                            model.setSliderPosition()
                        }
                    }
                }
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
        }
    }

    private fun trackSelectionAction(
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
                    model.setSliderPosition()
                } else {
                    spotifyAppRemote?.let { remote ->
                        model.isPaused(false) // TODO: main select
                        model.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        model.handlePlayerActions(remote, action)
                    }
                    model.playbackPosition(0)
                    model.cancelJobIfRunning()
                    model.setSliderPosition()
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
    private fun writeToPreferences(token: String, prefKey: String) {
        val pref = this@MainActivity.getSharedPreferences(prefKey, Context.MODE_PRIVATE)
        with (pref.edit()) {
            putString(prefKey, token)
            apply()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun showSheet(
        scope: CoroutineScope,
        sheetState: SheetState,
        onShowSheet: (Boolean) -> Unit,
    ) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onShowSheet(false)
            }
        }
    }

    companion object {
        val TRACK_ID_KEY = "trackId"
        val INDEX_KEY = "index"
        val PLAYLIST_ID_KEY = "playlist_id/"
        val PLAYLIST_NAME_KEY = "playlist_name/"
        val MAIN_SCREEN_KEY = "main_screen"
        val PLAYLIST_SCREEN_KEY = "playlist_screen/"
        val SONG_DETAILS_KEY = "song_details/"
        val TAG = MainActivity::class.qualifiedName
        const val SPOTIFY_TOKEN = "SPOTIFY_TOKEN"
        private val AUTH_TOKEN_REQUEST_CODE = 0x10
        private val AUTH_CODE_REQUEST_CODE = 0x11
        private  val  REDIRECT_URI = "com.mccarty.ritmo://auth"
        private val IMAGE_URL = "https://i.scdn.co/image/"
        private var accessCode = ""
        const val TICKER_DELAY = 1_000L
    }
}
