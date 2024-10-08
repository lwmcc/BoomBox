package com.mccarty.ritmo.domain.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.IBinder
import android.os.Parcelable
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mccarty.ritmo.KeyConstants
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainActivity.Companion.INTENT_ACTION
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.RemoteServiceControls
import com.mccarty.ritmo.domain.SpotifyRemoteWrapper
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.MainActivity.Companion.TICKER_DELAY as TICKER_DELAY
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class PlaybackService: LifecycleService() {

    @Inject
    lateinit var remoteServiceControls: RemoteServiceControls

    private val binder = PlaybackBinder()

    @Inject
    lateinit var spotifyAppRemote: SpotifyRemoteWrapper

    private lateinit var backgroundPlayJob: Job
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var isActive = false
    private val startIds = mutableListOf<Int>()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        isActive = true
        return binder
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        lifecycleScope.launch {
            spotifyAppRemote.let {
                SpotifyAppRemote.disconnect(it.spotifyConnection())
            }
        }
    }

    /**
     * Check if called from Spotify notification
     *
     * Check [startId] if called from the Spotify notification then, do nothing.
     * If called from this app then show or update the notification
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (this::spotifyAppRemote.isInitialized) {
            lifecycleScope.launch {
                spotifyAppRemote.spotifyConnection().playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
                    val notification = createNotification(playerState)
                    ServiceCompat.startForeground(
                        this@PlaybackService,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                    )

                    if (!startIds.contains(startId)) {
                        startIds.add(startId)
                        val notificationManager = getSystemService(NotificationManager::class.java)
                        notificationManager.createNotificationChannel(createNotificationChannel())
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
            }
        }

        return START_STICKY
    }

    // TODO: can get from activity
    fun currentUri(currentUri: (String) -> Unit) {
        lifecycleScope.launch {
            spotifyAppRemote.spotifyConnection().let { remote ->
                remote.playerApi.playerState.setResultCallback { playerState ->
                    currentUri(playerState.track?.uri.toString())
                }
            }
        }
    }

    /**
     * This job for keeping track of play state while app is playing in background
     */
    fun cancelBackgroundPlayJob() {
        if (this::backgroundPlayJob.isInitialized && backgroundPlayJob.isActive) {
            backgroundPlayJob.cancel()
        }
    }

    fun tracksHasEnded(playlistData: MainActivity.PlaylistData) {
        scope.launch {
            while (isActive) {
                delay(TICKER_DELAY)
                lifecycleScope.launch {
                    spotifyAppRemote.spotifyConnection().let { remote ->
                        remote.playerApi.playerState?.setResultCallback { playerState ->
                            if (playerState.playbackPosition.milliseconds.inWholeSeconds == (playerState.track?.duration?.milliseconds?.inWholeSeconds?.minus(
                                    1L
                                ))
                            ) {
                                val index =
                                    playlistData.playlist.indexOfFirst { it.uri == playerState.track?.uri } + 1
                                if (playlistData.playlist.size == 1) {
                                    remote.playerApi.play(null)
                                } else if (playerState.track?.uri == playlistData.playlist[playlistData.playlist.size - 1].track?.uri.toString()) {
                                    remote.playerApi.play(playlistData.playlist[0].track?.uri)
                                } else {
                                    remote.playerApi.play(playlistData.playlist[index].track?.uri)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun isCurrentlyPlaying(isPlaying: (Boolean) -> Unit) {
        lifecycleScope.launch {
            spotifyAppRemote.spotifyConnection().let { remote ->
                remote.playerApi.playerState.setResultCallback { playerState ->
                    isPlaying(!playerState.isPaused)
                }
            }
        }
    }

    /**
     * All of the data needed to setup track information
     * from the Spotify API
     */
    fun sendBroadCast(playerState: PlayerState) {
        Intent().also { intent ->
            intent.setAction(INTENT_ACTION)
            intent.putExtra(
                PLAYER_STATE,
                Player(
                    imageUri = playerState.track?.imageUri.toString(),
                    trackArtist = playerState.track?.artist?.name,
                    albumName = playerState.track?.album?.name,
                    trackName = playerState.track?.name,
                    trackUri = playerState.track.uri,
                    isTrackPaused = playerState.isPaused,
                    position = playerState.playbackPosition,
                    duration = playerState.track.duration,
                ),
            )
            sendBroadcast(intent)
        }
    }

    /**
     * Information about the track that was playing while
     * app was in background. Call this before the job is cancelled
     */
/*    fun getTrackData(trackData: (track: Track?) -> Unit) {
        spotifyAppRemote?.playerApi?.playerState?.setResultCallback { playerState ->
            trackData(playerState.track)
        }?.setErrorCallback { throwable ->
            Timber.e(throwable.message)
        }

    }*/
/*
    fun handlePlayerActions(action: TrackSelectAction.TrackSelect) {
        remoteServiceControls.onTrackSelected(spotifyAppRemote, action)
    }*/

    fun getCurrentPosition(): Flow<Long> {
        return flow {
            lifecycleScope.launch {
                spotifyAppRemote.spotifyConnection().playerApi.playerState?.setResultCallback { playerState ->
                    scope.launch {
                        while (isActive) {
                            emit(playerState.playbackPosition)
                            delay(1_000)
                        }
                    }
                }
            }
        }.flowOn(Dispatchers.IO) // TODO: inject dispatcher
    }

    private fun createNotificationChannel(): NotificationChannel {
       return NotificationChannel(
            CHANNEL_ID,
            getString(R.string.track_playback_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).also { it.description = getString(R.string.track_playback_channel_description) }
    }

    private fun createNotification(playerState: PlayerState): Notification {
        val imageUrl = StringBuilder().apply {
            append(MainActivity.IMAGE_URL)
            append(playerState.track?.imageUri.toString().drop(22)?.dropLast(2)) // TODO: revisit
        }.toString()

        val layout = RemoteViews(applicationContext.packageName, R.layout.player_notification_layout)
        layout.setTextViewText(R.id.track_title, playerState.track?.artist?.name)
        layout.setTextViewText(R.id.artist, playerState.track?.name)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(
                Icon.createWithBitmap(
                    Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get()
                )
            )
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(layout)
            .setSilent(true)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(playerState.track?.artist?.name)
            )
            .setShowWhen(false)
            .build()
    }

    inner class PlaybackBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    companion object {
        const val CHANNEL_ID = "playback-service-channel-id"
        const val NOTIFICATION_ID = 888
        const val PLAYER_STATE = "player-state"
        const val TRACK_DATA = "track-data"
    }

    @Parcelize
    data class Player(
        val imageUri: String?,
        val trackArtist: String?,
        val albumName: String?,
        val trackName: String?,
        val trackUri: String?,
        val isTrackPaused: Boolean = true,
        val position: Long = 0L,
        val duration: Long = 0L,
    ) : Parcelable
}