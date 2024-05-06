package com.mccarty.ritmo.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainActivity
import com.bumptech.glide.integration.compose.GlideImage as GlideImage
import com.mccarty.ritmo.MainViewModel.RecentlyPlayedMusicState.Success as Success
import com.mccarty.ritmo.MainViewModel.PlaylistState.Success as PlaylistSuccess
import com.mccarty.ritmo.R
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.model.payload.Item
import com.skydoves.landscapist.glide.GlideImage
import java.net.URL

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainScreen(
    model: MainViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val recentlyPlayedMusic by model.recentlyPlayedMusic.collectAsStateWithLifecycle()
    val playList by model.playlist.collectAsStateWithLifecycle()
    val musicHeader by model.musicHeader.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            MainHeader(
                imageUrl = musicHeader.imageUrl.toString(),
                artistName = musicHeader.artistName,
                albumName = musicHeader.albumName,
                songName = musicHeader.songName,
                modifier = Modifier,
            )
        }

        when (recentlyPlayedMusic) {
            is MainViewModel.RecentlyPlayedMusicState.Pending -> {
                println("MainScreen ***** PENDING")
            }

            is Success<*> -> {
                item {
                    MediaList((recentlyPlayedMusic as Success<*>).data.items) {
                        navController.navigate("${MainActivity.SONG_DETAILS_KEY}${it.track.id}")
                    }
                }
            }

            else -> {
                println("MainScreen ***** ERROR")
            }
        }

        when (playList) {
            is MainViewModel.PlaylistState.Pending -> {
                println("MainScreen ***** PLAYLIST PENDING")
            }

            is PlaylistSuccess -> {
                val playlist = (playList as MainViewModel.PlaylistState.Success).playList

                if (playlist.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.playlists),
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 40.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
                for (item in playlist) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                .clickable(onClick = {
                                    navController.navigate("playlist_screen")
                                }),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Column() {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .paddingFromBaseline(top = 25.dp)
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
                                    text = "${stringResource(R.string.total_tracks)} ${item.tracks.total}",
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
            }

            else -> {
                println("MainScreen ***** PLAYLIST ERROR")
            }
        }
    }
}