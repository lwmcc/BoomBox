package com.mccarty.ritmo

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.State
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.domain.SpotifyRemoteWrapper
import com.mccarty.ritmo.domain.model.MusicHeader
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.domain.services.PlaybackService
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.ui.MainComposeScreen
import com.mccarty.ritmo.ui.theme.BoomBoxTheme
import com.mccarty.ritmo.utils.positionProduct
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import com.mccarty.ritmo.viewmodel.PlayerViewModel
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val mainViewModel: MainViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private var accessCode: String? = null

    private lateinit var playbackService: PlaybackService

    //lateinit var spotifyAppRemote: SpotifyAppRemote

    @Inject
    lateinit var spotifyAppRemote: SpotifyRemoteWrapper

    private var bound: Boolean = false

    private lateinit var receiver :  PlaybackServiceReceiver

    val CHANNEL = "com.mccarty.app/channel"
   // private lateinit var methodChannel: MethodChannel
   // private lateinit var flutterEngine: FlutterEngine

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(component: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.PlaybackBinder
            playbackService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(component: ComponentName?) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            connect(spotifyAppRemote.spotifyConnection())
        }

        FirebaseApp.initializeApp(this)
        firebaseAnalytics = Firebase.analytics

        receiver = PlaybackServiceReceiver()
        ContextCompat.registerReceiver(this, receiver, IntentFilter(INTENT_ACTION), ContextCompat.RECEIVER_EXPORTED)
        setContent {
            BoomBoxTheme {
                Scaffold { padding ->
                    MainComposeScreen(
                        mainViewModel = mainViewModel,
                        padding = padding,
                        viewMore = getString(R.string.sheets_view_more),
                        // Track select in playlist
                        mediaEvents = object : MediaEvents { // TODO: refactor this get rid of that object
                            override fun trackSelectionAction(
                                trackSelectAction: TrackSelectAction,
                                isPaused: State<Boolean>,
                            ) {
                                trackSelection(trackSelectAction, isPaused.value)
                            }
                        },
                        onPlaylistSelectAction = {
                            mainViewModel.setPlaylistId(it)
                        },
                        onPlayerControlAction = {
                            playerControlAction(it)
                        },
                    )
                }
            }
        }

        if (savedInstanceState == null) {
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, getAuthenticationRequest())
        }
    }

    override fun onStart() {
        super.onStart()
       /* Intent(this, PlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }*/
    }

    override fun onResume() {
        super.onResume()

        if (this::playbackService.isInitialized) {
            playbackService.currentUri { currentUri ->

                val playlistName = mainViewModel.playlistData.value?.name

                playbackService.isCurrentlyPlaying { isPlaying ->
                    val trackIndex = mainViewModel.getTrackIndex(currentUri)

                    if (isPlaying) {
                        when(playlistName) {
                            PlaylistNames.RECENTLY_PLAYED, PlaylistNames.USER_PLAYLIST -> {
                                mainViewModel.setPlaylistData(
                                    mainViewModel.playlistData.value?.copy(
                                        uri = currentUri,
                                        index = trackIndex,
                                    )
                                )
                            }

                            else -> {
                                mainViewModel.setPlaylistData(
                                    Playlist(
                                        uri = currentUri,
                                        index = 0,
                                        name = PlaylistNames.RECENTLY_PLAYED,
                                        tracks = mainViewModel.recommendedPlaylist,
                                    )
                                )
                            }
                        }
                    }
                }
            }

            lifecycleScope.launch {
                spotifyAppRemote.spotifyConnection().playerApi.playerState.setResultCallback { playerState ->
                    playerState.track.let {
                        mainViewModel.setBackgroundTrackData(TrackData(it.uri))
                    }
                }
            }

            playbackService.cancelBackgroundPlayJob()
/*            playbackService.currentUri { currentUri ->
               playerViewModel.setCurrentUri(currentUri)
            }*/

            // TODO: testing only
            //getCurrentPosition()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::playbackService.isInitialized) {
            when (mainViewModel.playlistData.value?.name) {
                PlaylistNames.RECENTLY_PLAYED -> {
                    playbackService.tracksHasEnded(PlaylistData(playlist = mainViewModel.recentlyPlayedMusic()))
                }

                PlaylistNames.USER_PLAYLIST -> {
                    playbackService.tracksHasEnded(PlaylistData(playlist = mainViewModel.currentPlaylist))
                }

                else -> {
                    // TODO: to implement
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainViewModel.cancelJob()
        if (this::playbackService.isInitialized) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        lifecycleScope.launch {
            SpotifyAppRemote.disconnect(spotifyAppRemote.spotifyConnection())
        }
    }

    fun connect(appRemote: SpotifyAppRemote?) {
        appRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            playerViewModel.setPlayerState(
                PlaybackService.Player(
                    imageUri = playerState.track?.imageUri.toString(),
                    trackArtist = playerState.track?.artist?.name,
                    albumName = playerState.track?.album?.name,
                    trackName = playerState.track?.name,
                    trackUri = playerState.track.uri,
                    isTrackPaused = playerState.isPaused,
                    position = playerState.playbackPosition,
                    duration = playerState.track.duration,
                )
            )
        }
    }

    private fun setupTrackInformation(player: PlaybackService.Player) {
            mainViewModel.setMusicHeader(MusicHeader().apply {
                imageUrl = StringBuilder().apply {
                    append(IMAGE_URL)
                    append(player.imageUri?.drop(22)?.dropLast(2)) // TODO: revisit, move
                }.toString()
                artistName = player.trackArtist ?: getString(R.string.artist_name)
                albumName = player.albumName ?: getString(R.string.album_name)
                songName = player.trackName ?: getString(R.string.track_name)
                dataSet = true
            })

            mainViewModel.setTrackUri(player.trackUri)
            mainViewModel.isPaused(player.isTrackPaused)
            mainViewModel.fetchMainMusic()

            // TODO: handle error
            /*  }?.setErrorCallback {
                  mainViewModel.setMainMusicError(it?.message ?: "Could Not Connect to Spotify")
              }*/
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
            .setCampaign("your-campaign-token") // TODO: set this
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val response = AuthorizationClient.getResponse(resultCode, data)
        if (response?.accessToken != null) {
            writeToPreferences(response.accessToken)
            if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
                try {
                    fetchData()
                } catch (ioe: IOException) {
                    Timber.e(ioe.message ?: "Error on return from Spotify auth")
                }
            } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
                accessCode = response.code
            }
        }
    }

    private fun fetchData() {
        mainViewModel.fetchRecentlyPlayedMusic()

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.playerState.collect { player ->
                    player?.let { setupTrackInformation(it) }
                }
            }
        }
    }

    private fun getRedirectUri(): Uri? {
        return Uri.parse(REDIRECT_URI)
    }

    private fun playerControlAction(action: PlayerControlAction, trackEnded: Boolean = false) {
        when(action) {
            PlayerControlAction.Back -> {

                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().let { remote ->
                        remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                            mainViewModel.setLastPlayedTrackData(playerState.track)
                        }

                        mainViewModel.isPaused(false)
                        mainViewModel.setPlaybackPosition(0)
                        remote.playerApi.skipPrevious()
                    }
                }
            }

            is PlayerControlAction.Play -> {
                startPlaybackService()
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().let {
                        it.playerApi.playerState.setResultCallback { playerState ->
                            mainViewModel.resumePlayback(
                                position = playerState.playbackPosition.milliseconds.inWholeSeconds,
                                playerState = playerState,
                                remote = it,
                            )
                        }
                    }
                }
            }

            is PlayerControlAction.Seek -> {
                mainViewModel.cancelJob()
                mainViewModel.setPlaybackPosition(action.position.toInt())
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
                }
            }

            is PlayerControlAction.Skip -> {
                startPlaybackService()
                when(mainViewModel.playlistData.value?.name) {
                    PlaylistNames.RECENTLY_PLAYED -> {
                        if (!trackEnded) {
                            mainViewModel.cancelJob()
                        }
                        setupSliderPosition(INCREMENT_INDEX)
                    }
                    PlaylistNames.USER_PLAYLIST -> {
                        if (!trackEnded) {
                            mainViewModel.cancelJob()
                        }
                        setupSliderPosition(INCREMENT_INDEX)
                    }

                    else -> {
                        val track = mainViewModel.recentlyPlayedMusic().firstOrNull()?.track

                        lifecycleScope.launch {
                            spotifyAppRemote.spotifyConnection().let {
                                playerViewModel.handlePlayerActions(
                                    it,
                                    TrackSelectAction.TrackSelect(
                                        index = INITIAL_INDEX,
                                        duration = track?.duration_ms,
                                        uri = track?.uri,
                                        tracks = mainViewModel.recentlyPlayedMusic(),
                                        playlistName = PlaylistNames.RECENTLY_PLAYED,
                                    ),
                                )
                            }
                        }
                        mainViewModel.setPlaylistData(
                            Playlist(
                                uri = track?.uri,
                                index = INITIAL_INDEX,
                                name = PlaylistNames.RECENTLY_PLAYED,
                                tracks = mainViewModel.recentlyPlayedMusic(),
                            )
                        )
                    }
                }
            }

            is PlayerControlAction.PlayWithUri -> {
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().let {
                        it.playerApi.playerState.setResultCallback { playerState ->
                            mainViewModel.isPaused(playerState.isPaused)
                            if (playerState.isPaused) {
                                it.playerApi.play(action.uri)
                            } else {
                                it.playerApi.pause()
                            }
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
        isPaused: Boolean = true,
    ) {
        //startPlaybackService()
        when(action) {
            /**
             * Track has been selected from a playlist
             */
            is TrackSelectAction.TrackSelect -> {
                mainViewModel.isPaused(isPaused) // TODO: might not be needed
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().let {
                        playerViewModel.handlePlayerActions(it, action)
                        mainViewModel.setPlaylistData(
                            Playlist(
                                uri = action.uri,
                                index = action.index,
                                name = action.playlistName, // TODO: playlist track paused
                                tracks = action.tracks,
                            )
                        )
                    }
                }
            }
            is TrackSelectAction.PlayTrackWithUri -> {
                lifecycleScope.launch {
                    if (isPaused) {
                        spotifyAppRemote.spotifyConnection().playerApi.play(action.playTrackWithUri)
                    } else {
                        spotifyAppRemote.spotifyConnection().playerApi.pause()
                    }
                }
            }

            is TrackSelectAction.PlayTrackScrolledToWithUri -> {
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().playerApi.play(action.playTrackWithUri)
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

    // TODO: might not be needed as slider was moved to details
    private fun setupSliderPosition(index: Int = INITIAL_POSITION) {
        lifecycleScope.launch {
            spotifyAppRemote.spotifyConnection().let { remote ->
                when(mainViewModel.playlistData.value?.name) {
                    PlaylistNames.RECENTLY_PLAYED -> {
                        if (mainViewModel.checkIfIndexesEqual()) {
                            remote.playerApi.play(null)
                            mainViewModel.setPlaylistData(null)
                        } else {
                            val newIndex =  mainViewModel.newIndex(index)
                            val theUri = mainViewModel.getUri(newIndex)
                            mainViewModel.setPlaylistData(
                                mainViewModel.playlistData.value?.copy(
                                    uri = theUri,
                                    index =  newIndex,
                                )
                            )
                            mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                            mainViewModel.playbackDuration(mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.milliseconds?.inWholeSeconds)
                            remote.playerApi.play(theUri)

                            mainViewModel.setMusicHeaderUrl(
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.album?.images?.get(0)?.url,
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.artists?.get(0)?.name ?: getString(R.string.artist_name), // TODO: move duplicate code to own function
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.album?.name ?: getString(R.string.album_name),
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.name ?: getString(R.string.track_name),
                            )
                        }
                    }
                    PlaylistNames.USER_PLAYLIST -> {
                        if (mainViewModel.checkIfIndexesEqual()) {
                            remote.playerApi.play(null)
                            mainViewModel.setPlaylistData(null)
                        } else {
                            val newIndex =  mainViewModel.newIndex(index)
                            val theUri = mainViewModel.getUri(newIndex)

                            mainViewModel.setPlaylistData(
                                mainViewModel.playlistData.value?.copy(
                                    uri = theUri,
                                    index =  newIndex,
                                )
                            )
                            mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                            mainViewModel.playbackDuration(mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.duration_ms?.milliseconds?.inWholeSeconds)
                            remote.playerApi.play(theUri)

                            mainViewModel.setMusicHeaderUrl(
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.album?.images?.get(0)?.url,
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.artists?.get(0)?.name ?: getString(R.string.artist_name), // TODO: move duplicate code to own function
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.album?.name ?: getString(R.string.album_name),
                                mainViewModel.playlistData.value?.tracks?.get(newIndex)?.track?.name ?: getString(R.string.track_name),
                            )
                        }
                    }
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
                        // setupSliderPosition() // TODO: not using slider here
                    }
                }
            }
        }
    }

    private fun startPlaybackService() {
        this.startForegroundService(Intent(this, PlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        })
    }

    fun getCurrentPosition() {
        playbackService.isCurrentlyPlaying {
            if (it) {
                playbackService.getCurrentPosition()
            }
        }
    }

    data class PlaylistData(val playlist: List<MainItem> = emptyList())

    data class TrackData(val trackUri: String)

    companion object {
        const val INDEX_KEY = "index"
        const val PLAYLIST_NAME_KEY = "playlist_name"
        const val MAIN_SCREEN_KEY = "main_screen" // TODO: use enum
        const val PLAYLIST_SCREEN_KEY = "playlist_screen"
        const val PLAYLIST_ID_KEY = "playlist_id"
        const val SONG_DETAILS_KEY = "song_details/"

        const val SPOTIFY_TOKEN = "SPOTIFY_TOKEN"
        const val AUTH_TOKEN_REQUEST_CODE = 0x10
        const val AUTH_CODE_REQUEST_CODE = 0x11
        const  val  REDIRECT_URI = "com.mccarty.ritmo://auth"
        const val IMAGE_URL = "https://i.scdn.co/image/"
        const val TICKER_DELAY = 1_000L
        const val INCREMENT_INDEX = 1
        const val API_SEED_TRACKS = 2 // TODO: remove if not going to use
        const val API_SEED_ARTISTS = 3
        const val  INITIAL_POSITION = 0
        const val  INITIAL_INDEX = 0
        const val PAGER_SCROLL_DELAY = 1_000

        const val INTENT_ACTION = "com.mccarty.ritmo.PlayerState-Broadcast"

        val TAG = MainActivity::class.qualifiedName
    }

    interface MediaEvents {
        fun trackSelectionAction(trackSelectAction: TrackSelectAction, isPaused: State<Boolean>)
    }

    inner class PlaybackServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val player: PlaybackService.Player? = intent?.let {
                IntentCompat.getParcelableExtra(
                    it,
                    PlaybackService.PLAYER_STATE,
                    PlaybackService.Player::class.java,
                )
            }

            lifecycleScope.launch {
                player?.let {
                    playerViewModel.setPlayerState(player)
                }
            }
        }
    }
}
