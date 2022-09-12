package com.mccarty.ritmo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.codelab.android.datastore.AlbumPreference
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.data.AlbumPreferenceSerializer
import com.mccarty.ritmo.model.AlbumXX
import com.mccarty.ritmo.ui.screens.StartScreen
import com.mccarty.ritmo.ui.theme.BoomBoxTheme
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
    private var accessToken: String = ""
    private var accessCode = ""
    private val model: MainViewModel by viewModels()

    private val nav = NavHostController(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BoomBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    StartScreen()
                }
            }
        }

        lifecycleScope.launch {
            model.album.collect {
                saveAlbum(this@MainActivity, it)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.currentlyPlaying.collect { isPlaying ->
                    model.recentlyPlayed.collect { list ->
                        model.setMainHeader(isPlaying, list)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
        saveAlbum(MainActivity@ this, model.album.value)
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

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            model.setAuthToken(response.accessToken)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    model.getCurrentlyPlaying()
                    model.getRecentlyPlayed()
                    model.getLastPlayedSongId()
                    model.getPlaylists()
                }
            }

        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            accessCode = response.code
        }
    }

    private fun getRedirectUri(): Uri? {
        return Uri.parse(REDIRECT_URI)
    }

    private fun saveAlbum(context: Context, album: AlbumXX) {
        if (album.artists.isNotEmpty()) {
            lifecycleScope.launch {
                context.albumPreferenceDataStore.updateData {
                    it.toBuilder()
                        .setArtistName(album.artists[0].name)
                        .setAlbumName(album.name)
                        .setReleaseDate(album.release_date)
                        .setImageUrl(album.images[0].url)
                        .build()
                }
            }
        }
    }

    @OptIn(ExperimentalLifecycleComposeApi::class)
    @Composable
    fun songIsCurrentlyPlaying() {
        val currentlyPlaying: Boolean by model.currentlyPlaying.collectAsStateWithLifecycle()
        println("MainActivity Playing ${model.currentlyPlaying.value}")
    }

    val Context.albumPreferenceDataStore: DataStore<AlbumPreference> by dataStore(
        fileName = "album_settings.proto",
        serializer = AlbumPreferenceSerializer
    )

    val Context.dataStore by preferencesDataStore(name = "user_preferences")
}