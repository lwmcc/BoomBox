package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mccarty.ritmo.R
import com.mccarty.ritmo.ViewModel.MainViewModel
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.utils.getImageUrlFromList
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(model: MainViewModel) {
    val currentAlbum: CurrentlyPlaying by model.currentAlbum.collectAsStateWithLifecycle()
    val currentAlbumImageUrl: String by model.currentAlbumImageUrl.collectAsStateWithLifecycle()
    val recentlyPlayed: List<RecentlyPlayedItem> by model.recentlyPlayed.collectAsStateWithLifecycle()
    val playLists: List<PlaylistItem> by model.playLists.collectAsStateWithLifecycle()
    val queueItems: List<CurrentQueueItem> by model.queueItemList.collectAsStateWithLifecycle()
    val album: AlbumXX by model.album.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                    text = stringResource(R.string.currently_playing),
                    color = MaterialTheme.colors.primary,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
                        .fillMaxWidth(),
                )
            }
        } else { // Last album played
            if(album.images.isNotEmpty()) {
                item {
                    GlideImage(
                        imageModel = album.images.getImageUrlFromList(0),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(300.dp)
                    )
                }
                if(album.artists.isNotEmpty()) {
                    item {
                        Text(
                            text = album.artists[0].name,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .paddingFromBaseline(top = 40.dp)
                                .fillMaxWidth(),
                        )
                    }
                    item {
                        Text(
                            text = album.name,
                            fontStyle = FontStyle.Normal,
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
                    text = "${currentAlbum.album?.release_date}",
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
                    text = stringResource(R.string.music_queue),
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
        for(item in queueItems) {
            item {
                Text(
                    text = item.name,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 40.dp)
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