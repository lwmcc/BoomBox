package com.mccarty.ritmo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.domain.model.MusicHeader
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.domain.services.PlaybackService
import com.mccarty.ritmo.ui.MainComposeScreen
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.utils.positionProduct
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.viewmodel.PlayerViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    private var accessCode: String? = null

    private lateinit var playbackService: PlaybackService

    private var bound: Boolean = false

    lateinit var receiver :  PlaybackServiceReceiver

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
        receiver = PlaybackServiceReceiver()
        ContextCompat.registerReceiver(this, receiver, IntentFilter(INTENT_ACTION), ContextCompat.RECEIVER_EXPORTED)
        setContent {
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.height(100.dp),
                    ) {
                        PlayerControls(onAction = this@MainActivity::playerControlAction)
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
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, getAuthenticationRequest())
        }

        fetchData()
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()

        if (this::playbackService.isInitialized) {
            playbackService.tracksHasEnded(PlaylistData(playlist = mainViewModel.recentlyPlayedMusic()))
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::playbackService.isInitialized) {
            playbackService.getTrackData { trackData ->
                trackData?.let {
                    mainViewModel.setBackgroundTrackData(TrackData(it.uri))
                }
            }
            playbackService.cancelJob()
        }
    }


    override fun onStop() {
        super.onStop()
        mainViewModel.cancelJob()
        unbindService(connection)
        bound = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun setupTrackInformation(player: PlaybackService.Player, bound: Boolean) {
        if (bound) {
            mainViewModel.setMusicHeader(MusicHeader().apply {
                imageUrl = StringBuilder().apply {
                    append(IMAGE_URL)
                    append(player.imageUri?.drop(22)?.dropLast(2)) // TODO: revisit
                }.toString()
                artistName = player.trackArtist ?: getString(R.string.artist_name)
                albumName = player.albumName ?: getString(R.string.album_name)
                songName = player.trackName ?: getString(R.string.track_name)
            })

            mainViewModel.setTrackUri(player.trackUri)
            mainViewModel.isPaused(player.isTrackPaused)
            mainViewModel.fetchMainMusic()
            /*  }?.setErrorCallback {
                  mainViewModel.setMainMusicError(it?.message ?: "Could Not Connect to Spotify")
              }*/
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
                    mainViewModel.fetchRecentlyPlayedMusic()
                } catch (ioe: IOException) {
                    Timber.e(ioe.message ?: "Error on return from Spotify auth")
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.playerState.collect { player ->
                    setupTrackInformation(player, bound)
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
                playbackService.remote()?.let { remote ->
                    remote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
                        mainViewModel.setLastPlayedTrackData(playerState.track)
                    }

                    mainViewModel.isPaused(false)
                    mainViewModel.setPlaybackPosition(0)
                    remote.playerApi.skipPrevious()
                }
            }

            is PlayerControlAction.Play -> {
                startPlaybackService()
                playbackService.remote()?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        val test = playerState.playbackPosition.milliseconds.toLong(DurationUnit.SECONDS)
                        mainViewModel.resumePlayback(
                            position = playerState.playbackPosition.milliseconds.inWholeSeconds,
                            playerState = playerState,
                            remote = it,
                        )
                    }
                }
            }

            is PlayerControlAction.Seek -> {
                mainViewModel.cancelJob()
                mainViewModel.setPlaybackPosition(action.position.toInt())
                playbackService.remote()?.playerApi?.seekTo(action.position.positionProduct(TICKER_DELAY))
            }

            is PlayerControlAction.Skip -> {
                startPlaybackService()
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
                    else -> { setupSliderPosition(INCREMENT_INDEX) }
                }
            }

            is PlayerControlAction.PlayWithUri -> {
                startPlaybackService()
                playbackService.remote()?.let {
                    it.playerApi.playerState.setResultCallback { playerState ->
                        mainViewModel.isPaused(playerState.isPaused)
                        if (playerState.isPaused) {
                            playbackService.remote()?.playerApi?.play(action.uri)
                        } else {
                            playbackService.remote()?.playerApi?.pause()
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
        startPlaybackService()
        when(action) {
            is TrackSelectAction.TrackSelect -> {
                mainViewModel.setMusicHeaderUrl(
                    action.tracks[action.index].track?.album?.images?.get(0)?.url,
                    action.tracks[action.index].track?.artists?.get(0)?.name ?: getString(R.string.artist_name),
                    action.tracks[action.index].track?.album?.name ?: getString(R.string.album_name),
                    action.tracks[action.index].track?.name ?: getString(R.string.track_name),
                )
                if (isPaused.value) {
                    playbackService.remote()?.let { remote ->
                        mainViewModel.isPaused(false)
                        mainViewModel.playbackDuration(action.tracks[action.index].track
                            ?.duration_ms?.milliseconds?.inWholeSeconds)
                        mainViewModel.handlePlayerActions(remote, action)
                    }
                    mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                    mainViewModel.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = action.playlistName,
                            tracks = action.tracks,
                        )
                    )
                } else {
                    mainViewModel.isPaused(false)
                    mainViewModel.playbackDuration(action.tracks[action.index].track?.duration_ms?.milliseconds?.inWholeSeconds)
                    playbackService.remote()?.let { remote ->
                        mainViewModel.handlePlayerActions(remote, action)
                    }
                    mainViewModel.cancelJob()
                    mainViewModel.setPlaybackPosition(INITIAL_POSITION)
                    mainViewModel.setPlaylistData(
                        Playlist(
                            uri = action.uri,
                            index = action.index,
                            name = action.playlistName,
                            tracks = action.tracks,
                        )
                    )
                }
            }
            is TrackSelectAction.PlayTrackWithUri -> {
                if (isPaused.value) {
                    playbackService.remote()?.playerApi?.play(action.playTrackWithUri)
                    mainViewModel.isPaused(false)
                } else {
                    playbackService.remote()?.playerApi?.pause()
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
        playbackService.remote()?.let { remote ->
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
                    setupSliderPosition()
                }
            }
        }
    }

    private fun startPlaybackService() {
        this.startForegroundService(Intent(this, PlaybackService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        })
    }

    private fun notificationChannel() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED -> {
                // User allowed
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                println("MainActivity ***** shouldShowRequestPermissionRationale")
                // User granted then removed
            }

            else -> {
                println("MainActivity ***** not granted")
                // First load
                // on start
            }

        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(notificationManager.areNotificationsEnabled()) {

            println("MainActivity ***** ENABLED")
        } else {
            println("MainActivity ***** NOT ENABLED")
        }

        val notification = NotificationCompat.Builder(this, PlaybackService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentTitle("mccarty title")
            .setContentText("larry text will be here")
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name ="larry mccarty"
            val desc = "what a description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(PlaybackService.CHANNEL_ID, name, importance).apply {
                description = desc
            }

            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(PlaybackService.NOTIFICATION_ID, notification)
        }
    }

    fun notificationManager() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun launchPlayerNotificaiton(manager: NotificationManager) {
        val not = manager.areNotificationsEnabled()
        val mine = manager.activeNotifications
        this.packageName
        // NOTIFICATION_ID
        mine.forEach {
            println("MainActivity ***** KEY ${it.key} MINE ${this.packageName}")
        }

    }

    data class PlaylistData(val playlist: List<MainItem> = emptyList())

    data class TrackData(val trackUri: String)

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
