package com.mccarty.ritmo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.model.MusicHeader
import com.mccarty.ritmo.ui.screens.StartScreen
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val AUTH_TOKEN_REQUEST_CODE = 0x10
    val AUTH_CODE_REQUEST_CODE = 0x11
    val REDIRECT_URI = "com.mccarty.ritmo://auth"
    private var accessCode = ""
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO: not working with material 3
            //BoomBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartScreen()
                }
           // }
        }

/*        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.recentlyPlayed.collect { list ->
                    //val isPlaying = async { model.currentlyPlaying.value }.await()
                    //model.setMainHeader(true, list)
                    //model.setImageforHeader(list)
                }
            }

    }*/

        if (savedInstanceState == null) {
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
            AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
        }

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                appRemote.playerApi.subscribeToPlayerState().setEventCallback {

                    val isPlaying = !it.isPaused
                    if(isPlaying) {
                        model.fetchLastPlayedSong()
                    }

                    model.setMusicHeader(MusicHeader().apply {
                        this.imageUrl = it.track.imageUri ?: null
                        this.artistName = it.track.artist.name ?: ""
                        this.albumName = it.track.album.name ?: ""
                        this.songName = it.track.name ?: ""
                    })
                }

                //Log.d("MainActivity", "Connected! Yay!")
                // Now you can start interacting with App Remote
                //connected()
            }

            override fun onFailure(throwable: Throwable) {
                println("SpotifyBroadcastReceiver ***** ${throwable.message}")
            }
        })

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
        if (requestCode != null && response?.accessToken != null) {
            if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
                model.setAuthToken(this, response.accessToken)


                // TODO: moved methods to VM, will populate UI from there
/*               lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        model.getCurrentlyPlaying()
                        model.getRecentlyPlayed()
                        model.getLastPlayedSongId()
                        model.getPlaylists()
                    }
                }*/
            } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
                accessCode = response.code
            }
        }
    }

    private fun getRedirectUri(): Uri? {
        return Uri.parse(REDIRECT_URI)
    }

    interface AuthTokenSet {
        fun fetchRecentlyPlayed()
    }
}