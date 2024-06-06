package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mccarty.ritmo.MainViewModel.AllPlaylistsState.Success as PlaylistSuccess
import com.mccarty.ritmo.R
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.ui.PlayList
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    model: MainViewModel,
    onViewMoreClick: (Boolean, Int, List<TrackDetails>) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val recentlyPlayedMusic by model.recentlyPlayedMusic.collectAsStateWithLifecycle()
    val allPlayLists by model.allPlaylists.collectAsStateWithLifecycle()
    val musicHeader by model.musicHeader.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        when (allPlayLists) {
            is MainViewModel.AllPlaylistsState.Pending -> {
                item {
                    CircleSpinner(32.dp)
                }
            }

            is PlaylistSuccess -> {
                item {
                    MainHeader(
                        imageUrl = musicHeader.imageUrl.toString(),
                        artistName = musicHeader.artistName,
                        albumName = musicHeader.albumName,
                        songName = musicHeader.songName,
                        modifier = Modifier,
                    )
                }
                val tracks = (recentlyPlayedMusic as Success).trackDetails
                if (tracks.isNotEmpty()) {
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
                itemsIndexed(tracks) { index, track ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    onAction(
                                        TrackSelectAction.TrackSelect(
                                            index,
                                            tracks[index].uri,
                                            tracks
                                        )
                                    )
                                }
                            )
                            .padding(5.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val imageUrl = track.images.firstOrNull()?.url
                            GlideImage(
                                model = imageUrl,
                                contentDescription = "", // TODO: add description
                                modifier = Modifier.size(100.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .weight(1f),

                                ) {
                                Text(
                                    text = track.trackName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .paddingFromBaseline(top = 25.dp)
                                        .fillMaxWidth(),
                                )
                                Text(
                                    text = track.albumName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .paddingFromBaseline(top = 25.dp)
                                        .fillMaxWidth()
                                )
                                if (track.artists.isNotEmpty()) {
                                    Text(
                                        text = track.artists[0].name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .paddingFromBaseline(top = 25.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    id = R.string.icon_view_more,
                                ),
                                modifier = Modifier.clickable {
                                    onViewMoreClick(true, index, tracks)
                                    onAction(TrackSelectAction.ViewMoreSelect(index, tracks))
                                }
                            )
                        }
                    }
                }

                item {
                    MediaList(
                        tracks,
                        onViewMoreClick = { showBottom, index, tracks ->
                            model.setPlayList(tracks)
                            onViewMoreClick(showBottom, index, tracks)
                        },
                        onAction = {
                           onAction(it)
                        }
                    )
                }

                val playlist = (allPlayLists as MainViewModel.AllPlaylistsState.Success).playLists
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

                itemsIndexed(playlist) {index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .clickable(onClick = {
                                model.fetchPlaylist(playlist[index].id)
                                navController.navigate("${MainActivity.PLAYLIST_SCREEN_KEY}${playlist[index].id}")
                            }),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        val imageUrl = item.images.firstOrNull()?.url
                        Row {
                            GlideImage(
                                model = imageUrl,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(100.dp),
                            )

                            Column(modifier = Modifier.padding(start = 20.dp)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .paddingFromBaseline(top = 25.dp)
                                        .fillMaxWidth(),
                                )
                                if (item.description.isNotEmpty()) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .paddingFromBaseline(top = 25.dp)
                                            .fillMaxWidth(),
                                    )
                                }
                                Text(
                                    text = "${stringResource(R.string.total_tracks)} ${item.tracks.total}",
                                    style = MaterialTheme.typography.bodyMedium,
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