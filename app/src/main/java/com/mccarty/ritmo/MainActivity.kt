package com.mccarty.ritmo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mccarty.ritmo.KeyConstants.CLIENT_ID
import com.mccarty.ritmo.ViewModel.MainViewModel
import com.mccarty.ritmo.model.*
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
    val currentAlbum: CurrentlyPlaying by model.currentAlbum.collectAsStateWithLifecycle()
    val currentAlbumImageUrl: String by model.currentAlbumImageUrl.collectAsStateWithLifecycle()
    val recentlyPlayed: List<RecentlyPlayedItem> by model.recentlyPlayed.collectAsStateWithLifecycle()
    val playLists: List<PlaylistItem> by model.playLists.collectAsStateWithLifecycle()
    val queueItems: List<CurrentQueueItem> by model.queueItemList.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally

        ) {
        // Currently Playing
        if(currentAlbum.artists.isNotEmpty()) {
            item {
                GlideImage(
                    imageModel = currentAlbumImageUrl,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(300.dp)
                )
            }
            item {
                Text(
                    text = "Currently Playing",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }
        for(artist in currentAlbum.artists) {
            item {
                Text(
                    text = "${artist.name}",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
        }
        if(currentAlbum.artists.isNotEmpty()) {
            item {
                Text(
                    text = "${currentAlbum.name}",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
            item {
                Text(
                    text = "${currentAlbum.album?.name}",
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
            item {
                Text(
                    text = "Release Date: ${currentAlbum.album?.release_date}",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),)
            }
        }

        if(queueItems.isNotEmpty()) {
            // Queue
            item {
                Text(
                    text ="Music Queue",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }
        for(item in queueItems) {
            item {
                Text(
                    text = item.name,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
                Text(
                    text = item.album.name,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
        }

        // Recently Played
        if(recentlyPlayed.isNotEmpty()) {
            item {
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
            item {
                Text(
                    text ="Recently Played",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }
        for(item in recentlyPlayed) {
            item {
                Text(
                    text = item.track.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
                Text(
                    text = item.track.album.name,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
        }
        if(recentlyPlayed.isNotEmpty()) {
            item {
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }

        // Playlists
        if(playLists.isNotEmpty()) {
            item {
                Text(
                    text ="Playlists",
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }
        for(item in playLists) {
            item {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth(),
                    )
                }
                Text(
                    text = "Total Tracks: ${item.tracks.total}",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}