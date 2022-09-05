package com.mccarty.ritmo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.ViewModel.MainViewModel
import com.mccarty.ritmo.ui.screens.MainScreen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BoomBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(model)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(
            CLIENT_ID,
            type,
            getRedirectUri().toString()
        )
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-email", "user-read-recently-played", "user-read-playback-state"))
            .setCampaign("your-campaign-token")
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val response = AuthorizationClient.getResponse(resultCode, data)

        if(requestCode == AUTH_TOKEN_REQUEST_CODE) {
            model.setAuthToken(response.accessToken)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    model.getRecentlyPlayed()
                    model.getPlaylists()
                    model.getQueue()
                    model.getLastPlayedSongId()
                }
            }

        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            accessCode = response.code
        }
    }

    private fun getRedirectUri(): Uri? {
        return return Uri.parse(REDIRECT_URI)
    }
}