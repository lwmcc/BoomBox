package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.bumptech.glide.integration.compose.GlideImage as GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.*
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainScreen(
    model: MainViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val recentlyPlayed: List<TrackV2Item> by model.recentlyPlayed.collectAsStateWithLifecycle()
    val playLists: List<PlaylistItem> by model.playLists.collectAsStateWithLifecycle()
    val mainMusicHeader: MainMusicHeader by model.mainMusicHeader.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Header
        item {
            GlideImage(
                model = mainMusicHeader.imageUrl,
                contentDescription = "",
                modifier = Modifier.size(300.dp),
            )
        }
        item {
            Text(
                text = mainMusicHeader.artistName,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .paddingFromBaseline(top = 25.dp)
                    .fillMaxWidth(),
            )
        }
        item {
            Text(
                text = mainMusicHeader.albumName,
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
                text = mainMusicHeader.songName,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                modifier = Modifier
                    .paddingFromBaseline(top = 25.dp)
                    .fillMaxWidth(),
            )
        }
        // End Header

        // Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.recently_played),
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
        for (item in recentlyPlayed) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            navController.navigate("song_details")
                        })
                        .padding(5.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Column() {
                        Text(
                            text = "${item.track?.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth(),
                        )
                        Text(
                            text = "${item.track?.album?.name}",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth()
                        )
                        if (item.track?.artists?.isNotEmpty() == true) {
                            Text(
                                text = "${item.track.artists.get(0).name}",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .paddingFromBaseline(top = 25.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } // End recently played

        // Playlists
        if (playLists.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.playlists),
                    color =  MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        }
        for (item in playLists) {
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
        }  // End Playlists
    }
}