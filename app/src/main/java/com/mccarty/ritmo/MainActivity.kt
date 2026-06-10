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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val mainViewModel: MainViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private var accessCode: String? = null
    private var codeVerifier: String = ""

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
            try {
                connect(spotifyAppRemote.spotifyConnection())
            } catch (e: Exception) {
                Timber.e(e, "Spotify remote connection failed")
            }
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
            launchSpotifyAuth()
        }
    }

    private fun launchSpotifyAuth() {
        codeVerifier = generateCodeVerifier()
        getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
            .putString("code_verifier", codeVerifier).apply()
        val challenge = generateCodeChallenge(codeVerifier)
        val authUrl = Uri.Builder()
            .scheme("https").authority("accounts.spotify.com").path("/authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "user-read-email user-read-recently-played user-read-playback-state")
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .build().toString()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data ?: return
        if (uri.scheme != "com.mccarty.ritmo" || uri.host != "auth") return
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")
        if (error != null) {
            Timber.e("Spotify auth error: $error")
            return
        }
        if (code == null) return
        val verifier = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .getString("code_verifier", "") ?: ""
        lifecycleScope.launch {
            val token = exchangeCodeForToken(code, verifier)
            if (token != null) {
                writeToPreferences(token)
                fetchData()
                try {
                    connect(spotifyAppRemote.spotifyConnection())
                } catch (e: Exception) {
                    Timber.e(e, "Spotify remote connection failed after auth")
                }
            }
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
                try {
                    spotifyAppRemote.spotifyConnection().playerApi.playerState.setResultCallback { playerState ->
                        playerState.track.let {
                            mainViewModel.setBackgroundTrackData(TrackData(it.uri))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Spotify remote connection failed in onResume")
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
            try {
                SpotifyAppRemote.disconnect(spotifyAppRemote.spotifyConnection())
            } catch (e: Exception) {
                Timber.e(e, "Spotify remote disconnect failed")
            }
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

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private suspend fun exchangeCodeForToken(code: String, verifier: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val params = "grant_type=authorization_code" +
                    "&code=${URLEncoder.encode(code, "UTF-8")}" +
                    "&redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}" +
                    "&client_id=${URLEncoder.encode(CLIENT_ID, "UTF-8")}" +
                    "&code_verifier=${URLEncoder.encode(verifier, "UTF-8")}"
                val connection = URL("https://accounts.spotify.com/api/token")
                    .openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true
                connection.outputStream.use { it.write(params.toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val json = connection.inputStream.bufferedReader().use { it.readText() }
                    JSONObject(json).getString("access_token")
                } else {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Timber.e("Token exchange HTTP ${connection.responseCode}: $error")
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Token exchange exception")
                null
            }
        }

    private fun fetchData() {
        mainViewModel.fetchRecentlyPlayedMusic()
        mainViewModel.fetchMainMusic()

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
                    try {
                        spotifyAppRemote.spotifyConnection().let { remote ->
                            remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                                mainViewModel.setLastPlayedTrackData(playerState.track)
                            }

                            mainViewModel.isPaused(false)
                            mainViewModel.setPlaybackPosition(0)
                            remote.playerApi.skipPrevious()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on Back")
                    }
                }
            }

            is PlayerControlAction.Play -> {
                startPlaybackService()
                lifecycleScope.launch {
                    try {
                        spotifyAppRemote.spotifyConnection().let {
                            it.playerApi.playerState.setResultCallback { playerState ->
                                mainViewModel.resumePlayback(
                                    position = playerState.playbackPosition.milliseconds.inWholeSeconds,
                                    playerState = playerState,
                                    remote = it,
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on Play")
                    }
                }
            }

            is PlayerControlAction.Seek -> {
                mainViewModel.cancelJob()
                mainViewModel.setPlaybackPosition(action.position.toInt())
                lifecycleScope.launch {
                    try {
                        spotifyAppRemote.spotifyConnection().playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on Seek")
                    }
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
                            try {
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
                            } catch (e: Exception) {
                                Timber.e(e, "Spotify remote error on Skip")
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
                    try {
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
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on PlayWithUri")
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
                    try {
                        spotifyAppRemote.spotifyConnection().let {
                            playerViewModel.handlePlayerActions(it, action)
                            mainViewModel.setPlaylistData(
                                Playlist(
                                    uri = action.uri,
                                    index = action.index,
                                    name = action.playlistName,
                                    tracks = action.tracks,
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on TrackSelect")
                    }
                }
            }
            is TrackSelectAction.PlayTrackWithUri -> {
                lifecycleScope.launch {
                    try {
                        if (isPaused) {
                            spotifyAppRemote.spotifyConnection().playerApi.play(action.playTrackWithUri)
                        } else {
                            spotifyAppRemote.spotifyConnection().playerApi.pause()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on PlayTrackWithUri")
                    }
                }
            }

            is TrackSelectAction.PlayTrackScrolledToWithUri -> {
                lifecycleScope.launch {
                    try {
                        spotifyAppRemote.spotifyConnection().playerApi.play(action.playTrackWithUri)
                    } catch (e: Exception) {
                        Timber.e(e, "Spotify remote error on PlayTrackScrolledToWithUri")
                    }
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
            try {
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
            } catch (e: Exception) {
                Timber.e(e, "Spotify remote error in setupSliderPosition")
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
