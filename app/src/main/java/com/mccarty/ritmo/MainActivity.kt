package com.mccarty.ritmo

//import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
//import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.ViewModel.MainViewModel
import com.mccarty.ritmo.model.CurrentAlbum
import com.mccarty.ritmo.model.PlaylistItem
import com.mccarty.ritmo.model.RecentlyPlayedItem
import com.mccarty.ritmo.ui.theme.BoomBoxTheme
import com.mccarty.ritmo.utils.convertBitmapFromDrawable
import com.skydoves.landscapist.glide.GlideImage
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
                    SetCurrentlyPlaying(model)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.recentlyPlayed.collect {
                    it.forEach {
                        println("MainActivity ${it.track.album.name}")
                        //it.track.name
                        //it.track.album.name
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.playLists.collect {
                    it.forEach {
                        println("MainActivity PL ${it.name}")
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.queueItemList.collect {
                    it.forEach {
                        println("MainActivity QUE ${it.name}")
                        //it.album?.name
                        //it.name
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.currentAlbum.collect {
                    println("MainActivity CA ${it.name}")
                    //it.album?.name
                    //it.album?.release_date
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

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@OptIn(ExperimentalLifecycleComposeApi::class)
//@Preview(showBackground = true)
@Composable
fun SetCurrentlyPlaying(model: MainViewModel) {
    val currentAlbum: CurrentAlbum by model.currentAlbum.collectAsStateWithLifecycle()
    val currentAlbumImageUrl: String by model.currentAlbumImageUrl.collectAsStateWithLifecycle()
    val recentlyPlayed: List<RecentlyPlayedItem> by model.recentlyPlayed.collectAsStateWithLifecycle()
    val playLists: List<PlaylistItem> by model.playLists.collectAsStateWithLifecycle()

    LazyColumn{
        item {
            GlideImage(
                imageModel = currentAlbumImageUrl,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(600.dp)
            )
        }

        currentAlbum.artists.forEach {
            item {
                Text(text = "${it.name}")
            }
        }

        item {
            Text(text = "${currentAlbum.name}")
        }

        item {
            Text(text = "${currentAlbum.album?.name}")
        }

        item {
            Text(text = "Release Date: ${currentAlbum.album?.release_date}")
        }

        // Recently Played
        item {
            Text(text ="Recently Played")
        }
        recentlyPlayed.forEach {
            item {
                Text(text = it.track.name)
                Text(text = it.track.album.name)
            }
        }

        // Playlists
        item {
            Text(text ="Play Lists")
        }
        playLists.forEach {
            item {
                Text(text = it.name)
                if (it.description.isNotEmpty()) {
                    Text(text = it.description)
                }
                Text(text = "Total Tracks: ${it.tracks.total.toString()}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BoomBoxTheme {
        Greeting("Android")
    }
}