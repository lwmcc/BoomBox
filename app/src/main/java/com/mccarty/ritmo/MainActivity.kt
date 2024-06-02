package com.mccarty.ritmo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.ui.BottomSheet
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.ui.screens.StartScreen
import com.mccarty.ritmo.viewmodel.PlayerAction
import com.mccarty.ritmo.viewmodel.TrackSelectAction
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        PlayerControls(onSlide = this@MainActivity::playerAction)
                    }
                }) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = padding.calculateTopPadding())
                    ) {
                        StartScreen(
                            navController,
                            onViewMoreClick = { bottomSheet, index ->
                                showBottomSheet = bottomSheet
                                trackIndex = index
                            },
                            onAction = {
                                trackSelectionAction(it)
                            },
                            onPlayPauseClicked = {
                                trackSelectionAction(it)
                            }
                        )
                    }
                }

                BottomSheet(
                    showBottomSheet,
                    sheetState = sheetState,
                    text = getString(R.string.sheets_view_more),
                    onDismiss = {
                        // TODO: duplicate code
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
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
    }

    private fun connect() {
        spotifyAppRemote?.let {
            it.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                val artistName = playerState.track.artist.name ?: null

                model.setMusicHeader(MusicHeader().apply {
                    this.imageUrl = StringBuilder().apply {
                        append(IMAGE_URL)
                        append(playerState.track.imageUri.toString().drop(22).dropLast(2))
                    }.toString()
                    this.artistName = artistName ?: "" // TODO: set strings null
                    this.albumName = playerState.track.album.name ?: ""
                    this.songName = playerState.track.name ?: ""
                })
                //TODO: model.setArtistName(artistName)
                model.setTrackUri(playerState.track.uri)
                model.isPaused(playerState.isPaused)
                model.playbackDuration(playerState.track.duration)

                lifecycleScope.launch(Dispatchers.IO) {
                    while (!playerState.isPaused) {
                        model.fetchPlaybackState()
                        delay(timeMillis = 1_000)
                    }
                }
            }
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
            if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
                try {
                    ApiClient.apply {
                        this.context = this@MainActivity
                        this.token = response.accessToken
                    }
                    //model.fetchCurrentlyPlaying() might not need this
                    model.fetchRecentlyPlayedMusic() // TODO: called above
                    //model.fetchLastPlayedSong() will use
                    //model.fetchAllPlaylists() // TODO: call from fetchRecentlyPlayedMusic
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

    companion object {
        val TRACK_ID_KEY = "trackId"
        val INDEX_KEY = "index"
        val PLAYLIST_ID_KEY = "playlist_id"
        val MAIN_SCREEN_KEY = "main_screen"
        val PLAYLIST_SCREEN_KEY = "playlist_screen/"
        val SONG_DETAILS_KEY = "song_details/"
        val TAG = MainActivity::class.qualifiedName
        private val AUTH_TOKEN_REQUEST_CODE = 0x10
        private val AUTH_CODE_REQUEST_CODE = 0x11
        private  val  REDIRECT_URI = "com.mccarty.ritmo://auth"
        private val IMAGE_URL = "https://i.scdn.co/image/"
        private var accessCode = ""
    }

    fun playerAction(action: PlayerAction) {
        when (action) {
            PlayerAction.Back -> {
                spotifyAppRemote?.playerApi?.skipPrevious()
            }

            PlayerAction.Play -> {
                if (model.isPaused.value) {
                    spotifyAppRemote?.playerApi?.resume()
                } else {
                    spotifyAppRemote?.playerApi?.pause()
                }
            }

            is PlayerAction.Seek -> {
                spotifyAppRemote?.playerApi?.seekTo(action.position.toLong())
            }

            PlayerAction.Skip -> {
                spotifyAppRemote?.playerApi?.skipNext()
            }
        }
    }

    fun trackSelectionAction(action: TrackSelectAction) {
        when(action) {
            is TrackSelectAction.DetailsSelect -> {}
            is TrackSelectAction.PlaylistTrackSelect -> {}
            is TrackSelectAction.RecentlyPlayedTrackSelect -> {}
            is TrackSelectAction.TrackSelect -> {
                println("MainActivity ***** TRACK SELECT")
            }
            is TrackSelectAction.ViewMoreSelect -> {
                println("MainActivity ***** VIEW MORE SELECT")
            }

            is TrackSelectAction.PlayTrackWithUri -> {
                if (model.isPaused.value) {
                    spotifyAppRemote?.playerApi?.play(action.playTrackWithUri)
                } else {
                    spotifyAppRemote?.playerApi?.pause()
                }
            }
        }
    }
}
