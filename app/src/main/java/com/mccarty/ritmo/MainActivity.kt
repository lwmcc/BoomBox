package com.mccarty.ritmo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.api.ApiClient
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.ui.screens.StartScreen
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            val artist = model.artistName.collectAsStateWithLifecycle().value.toString()
                            Text(artist)
                        }
                    )
                }) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(top = padding.calculateTopPadding())) {
                        StartScreen()
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
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
            it.playerApi.subscribeToPlayerState().setEventCallback {

                val artistName = it.track.artist.name ?: null

                model.setMusicHeader(MusicHeader().apply {
                    this.imageUrl = StringBuilder().apply {
                        append(IMAGE_URL)
                        append(it.track.imageUri.toString().drop(22).dropLast(2))
                    }.toString()
                    this.artistName = artistName ?: "" // TODO: set strings null
                    this.albumName = it.track.album.name ?: ""
                    this.songName = it.track.name ?: ""
                })
                model.setArtistName(artistName)
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
                    //model.fetchCurrentlyPlaying()
                    model.fetchRecentlyPlayedMusic()
                    //model.fetchLastPlayedSong()
                    model.fetchPlaylist()
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
        val MAIN_SCREEN_KEY = "main_screen"
        val PLAYLIST_SCREEN_KEY = "playlist_screen"
        val SONG_DETAILS_KEY = "song_details/"
        val TAG = MainActivity::class.qualifiedName
        private val AUTH_TOKEN_REQUEST_CODE = 0x10
        private val AUTH_CODE_REQUEST_CODE = 0x11
        private  val  REDIRECT_URI = "com.mccarty.ritmo://auth"
        private val IMAGE_URL = "https://i.scdn.co/image/"
        private var accessCode = ""
        private var spotifyAppRemote: SpotifyAppRemote? = null
    }
}
