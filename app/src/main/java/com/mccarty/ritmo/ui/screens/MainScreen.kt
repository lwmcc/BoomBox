package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.ui.material.surface.Surface
import com.codelab.android.datastore.AlbumPreference
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.R
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.utils.getImageUrlFromList
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    model: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
//    val currentAlbum: CurrentlyPlaying by model.currentAlbum.collectAsStateWithLifecycle()
//    val currentAlbumImageUrl: String by model.currentAlbumImageUrl.collectAsStateWithLifecycle()
    val recentlyPlayed: List<RecentlyPlayedItem> by model.recentlyPlayed.collectAsStateWithLifecycle()
    val playLists: List<PlaylistItem> by model.playLists.collectAsStateWithLifecycle()
    val mainMusicHeader: MainViewModel.MainMusicHeader by model.mainMusicHeader.collectAsStateWithLifecycle()
//  val queueItems: List<CurrentQueueItem> by model.queueItemList.collectAsStateWithLifecycle()
//  val album: AlbumXX by model.album.collectAsStateWithLifecycle()
    val currentlyPlaying: Boolean by model.currentlyPlaying.collectAsStateWithLifecycle()

    val lastPlayedArtist = model.artistName.observeAsState().value
    val lastPlayedAlbum: String? = model.albumName.observeAsState().value
    val lastImageUrl = model.imageUrl.observeAsState().value
//  val lastReleaseDate = model.releaseDate.observeAsState().value

    LazyColumn(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // Header
        item {
            GlideImage(
                imageModel = if (currentlyPlaying) mainMusicHeader.imageUrl ?: "" else lastImageUrl ?: "",
                contentScale = ContentScale.Fit,
                placeHolder = ImageBitmap.imageResource(R.drawable.default_music),
                error = ImageBitmap.imageResource(R.drawable.default_music),
                modifier = Modifier
                    .size(300.dp)
            )
        }
        item {
            Text(
                text = if (currentlyPlaying) mainMusicHeader.artistName ?: "" else lastPlayedArtist ?: "",
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
                text = if (currentlyPlaying) mainMusicHeader.albumName ?: "" else lastPlayedAlbum ?: "",
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
                text = if (currentlyPlaying) mainMusicHeader.songName ?: "" else "",
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
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
            }
            item {
                Text(
                    text = stringResource(R.string.recently_played),
                    color = MaterialTheme.colors.primary,
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
                        }),
                ) {
                    Column() {
                        Text(
                            text = "${item?.track?.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 40.dp)
                                .fillMaxWidth(),
                        )
                        Text(
                            text = "${item.track?.album?.name}",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        } // End recently played

        // Playlists
        if (playLists.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.playlists),
                    color = MaterialTheme.colors.primary,
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
                        .clickable(onClick = {
                            navController.navigate("playlist_screen")
                        }),
                ) {
                    Column() {
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