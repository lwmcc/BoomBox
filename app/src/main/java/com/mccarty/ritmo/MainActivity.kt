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
import com.mccarty.ritmo.model.RecentlyPlayed
import com.mccarty.ritmo.model.payload.PlaylistData
import com.mccarty.ritmo.ui.BottomSheet
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.ui.screens.StartScreen
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    private var spotifyAppRemote: SpotifyAppRemote? = null

    private var accessCode = ""

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

        println("MainActivity ***** onCreate")

        if (savedInstanceState == null) {
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
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

    fun fetchData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.trackUri.collect {
                    it?.let {
                        model.fetchRecentlyPlayedMusic()
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
                        println("MainActivity ***** POSITION 0")
                        model.playbackPosition(0)
                        spotifyAppRemote?.let { remote ->
                            remote.playerApi.playerState.setResultCallback { playerState ->
                                if (model.recentPlaylist?.name == PlaylistNames.RECENTLY_PLAYED) {
                                    if ((model.recentPlaylist?.index ?: 0) == (model?.recentPlaylist?.tracks?.lastIndex)) {
                                        remote.playerApi.play(null)
                                        model.setPlaylistData(null)
                                    } else {
                                        val newIndex = model.recentPlaylist?.index?.plus(1) ?: 0
                                        val theUri = model.recentPlaylist?.tracks!![newIndex].track?.uri.toString()
                                        model.setPlaylistData(
                                            Playlist(
                                                uri = theUri,
                                                index = newIndex,
                                                name = PlaylistNames.RECENTLY_PLAYED,
                                                tracks = emptyList(),
                                            )
                                        )
                                        model.recentPlaylist = model.recentPlaylist?.copy(
                                            uri = theUri,
                                            index = newIndex,
                                        )
                                        remote.playerApi.play(theUri)
                                    }
                                } else {
                                    remote.playerApi.skipNext()
                                }

                                model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                                model.setSliderPosition()
                                if (!playerState.isPaused) { model.cancelJobIfRunning() }
                                model.setMusicHeader(MusicHeader().apply {
                                    imageUrl = StringBuilder().apply {
                                        append(IMAGE_URL)
                                        append(playerState.track.imageUri.toString().drop(22).dropLast(2))
                                    }.toString()
                                    artistName = playerState.track.artist.name ?: getString(R.string.artist_name)
                                    albumName = playerState.track.album.name ?: getString(R.string.album_name)
                                    songName = playerState.track.name ?: getString(R.string.track_name)
                                }
                                )
                            }
                        }
                    }
                }
            }
        }

/*        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.playlistData.collectLatest {  playlistData ->

                    println("MainActivity ***** ******************")
                    println("MainActivity ***** PL INDEX ${playlistData?.index}") // ok
                    println("MainActivity ***** PL NAME ${playlistData?.name}") // playlist name ok
                    println("MainActivity ***** PL URI ${playlistData?.uri}") // track uri ok
                    playlistData?.tracks?.forEach { item ->
                        println("MainActivity ***** #######################")
                        // println("MainActivity ***** PL NAME 2 ${item.name}") // null
                        // println("MainActivity ***** PL URI 2 ${item.uri}") // null
                        println("MainActivity ***** PL TRACK NAME 2 ${item.track?.name}") // ok
                        println("MainActivity ***** PL TRACK NAME 2 ${item.track?.uri}")
                        println("MainActivity ***** PL TRACK NAME 2 ${item.track?.album?.name}")
                        //println("MainActivity ***** PL TRACKS NAME 3 ${item.trackName}") // null
                        println("MainActivity ***** #######################")
                    }
                    println("MainActivity ***** ******************")
                }
            }
        }*/

/*        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.currentlyPayingTrackState.collect { state ->
                    when(state) {
                        MainViewModel.CurrentlyPayingTrackState.Error ->  { }
                        is MainViewModel.CurrentlyPayingTrackState.Pending -> { }
                        is MainViewModel.CurrentlyPayingTrackState.Success<*> -> {
                            println("MainActivity ***** CURRENT")
                        }
                    }
                }
            }
        }*/
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
                model.cancelJobIfRunning()
                model.playbackPosition(action.position.toLong())
                spotifyAppRemote?.playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
                model.setSliderPosition()
            }

            is PlayerControlAction.Skip -> {
                spotifyAppRemote?.let { remote ->
                    remote.playerApi.playerState.setResultCallback { playerState ->
                        if (model.recentPlaylist?.name == PlaylistNames.RECENTLY_PLAYED) {
                            if ((model.recentPlaylist?.index ?: 0) == (model?.recentPlaylist?.tracks?.lastIndex)) {
                                remote.playerApi.play(null)
                                model.setPlaylistData(null)
                            } else {
                                val newIndex = model.recentPlaylist?.index?.plus(0) ?: 0
                                val theUri = model.recentPlaylist?.tracks!![newIndex].track?.uri.toString()

                                model.setPlaylistData(
                                    Playlist(
                                        uri = theUri,
                                        index = newIndex,
                                        name = PlaylistNames.RECENTLY_PLAYED,
                                        tracks = emptyList(),
                                    )
                                )

                                model.recentPlaylist = model.recentPlaylist?.copy(
                                    uri = theUri,
                                    index = newIndex,
                                )
                                remote.playerApi.play(theUri)
                            }
                        } else {
                            remote.playerApi.skipNext()
                        }

                        //model.cancelJobIfRunning()
                        model.playbackPosition(0)
                        model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                        // model.setSliderPosition(0, playerState.track.duration.quotientOf(TICKER_DELAY))
                        //model.startCancelJob()

                        //model.cancelJobIfRunning()
                        //model.playbackPosition(0)
                        model.setSliderPosition()
                        //model.playbackPosition(0f)
                        //model.playbackDuration(playerState.track.duration.quotientOf(TICKER_DELAY))
                        //if (!playerState.isPaused) {
                        //    model.cancelJobIfRunning()
                        //}
                        //model.setSliderPosition()
                    }
                }
                model.trackEnded(true)
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
                    model.setSliderPosition() // TODO: when not paying yet
                    // TODO: testing
                    model.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = action.tracks,
                        )
                    )

                    // TODO: remove what's not needed
                    model.recentPlaylist = Playlist(
                        uri = action.uri,
                        index = action.index,
                        name = PlaylistNames.RECENTLY_PLAYED,
                        tracks = action.tracks,
                    )
                } else {
                    spotifyAppRemote?.let { remote ->
                        model.isPaused(false) // TODO: main select
                        model.playbackDuration(action.tracks[action.index].track?.duration_ms?.quotientOf(TICKER_DELAY))
                        model.handlePlayerActions(remote, action)
                    }
                    model.playbackPosition(0)
                    //model.cancelJobIfRunning()
                    model.setSliderPosition()


                    // TODO: testing
                    model.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = PlaylistNames.RECENTLY_PLAYED,
                            tracks = action.tracks,
                        )
                    )

                    // TODO: remove what's not need
                    model.recentPlaylist = Playlist(
                        uri = action.uri,
                        index = action.index,
                        name = PlaylistNames.RECENTLY_PLAYED,
                        tracks = action.tracks,
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

            is TrackSelectAction.PlayTrackWithUri -> TODO()
            is TrackSelectAction.TrackSelect -> TODO()
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

        val TAG = MainActivity::class.qualifiedName
    }
}
