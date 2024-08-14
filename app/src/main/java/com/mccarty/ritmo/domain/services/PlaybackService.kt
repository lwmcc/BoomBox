package com.mccarty.ritmo.domain.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.mccarty.ritmo.KeyConstants
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainActivity.Companion.INTENT_ACTION
import com.mccarty.ritmo.R
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@AndroidEntryPoint
class PlaybackService: Service() {
    private val binder = PlaybackBinder()
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var accessCode: String? = null

    override fun onBind(intent: Intent): IBinder { return binder }

    override fun onCreate() {
        super.onCreate()
        SpotifyAppRemote.connect(this, ConnectionParams.Builder(KeyConstants.CLIENT_ID).apply {
            setRedirectUri(MainActivity.REDIRECT_URI)
            showAuthView(true)
        }.build(), object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                appRemote.playerApi.subscribeToPlayerState().setEventCallback {
                    sendBroadCast(it)
                }
            }

            override fun onFailure(throwable: Throwable) {
                println("PlaybackService ***** ERROR ${throwable.message}")
            }
        })

        notificationChannel()
    }

    override fun onDestroy() {

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        println("PlaybackService ***** onStartCommand()")

        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentTitle("mccarty title")
            .setContentText("larry text will be here")
            .build()

        val channel = NotificationChannel(
            "PLAYBACK_SERVICE",
            "BoomBox",
            NotificationManager.IMPORTANCE_HIGH,
        )

        channel.description = "PennSkanvTic channel for foreground service notification"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(NOTIFICATION_ID, notification)

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
        return START_STICKY
    }

    fun connect(): CallResult<PlayerState>? = spotifyAppRemote.let { it?.playerApi?.playerState }

    fun subscription(): Subscription<PlayerState>? = spotifyAppRemote.let { it?.playerApi?.subscribeToPlayerState() }

    fun remote() = spotifyAppRemote

    private fun notificationChannel(): NotificationManager {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name ="larry mccarty"
            val descriptionText = "what a description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
            return notificationManager
        }
        return notificationManager
    }

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
                    trackUri = playerState.track?.uri,
                    isTrackPaused = playerState.isPaused,
                    position = playerState.playbackPosition,
                    duration = playerState.track.duration,
                ),
            )
            sendBroadcast(intent)
        }
    }

    inner class PlaybackBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    companion object {
        const val CHANNEL_ID = "playback-service-channel-id"
        const val NOTIFICATION_ID = 888
        const val PLAYER_STATE = "player-state"
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