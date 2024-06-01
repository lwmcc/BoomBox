package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
                item {
                    MediaList(
                        tracks,
                        onTrackClick = { index, tracks ->
                            // TODO: play track
                        },
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
                item {
                    PlayList(playlist) { index ->
                        model.fetchPlaylist(playlist[index].id)
                        navController.navigate("${MainActivity.PLAYLIST_SCREEN_KEY}${playlist[index].id}")
                    }
                }
            }

            else -> {
                println("MainScreen ***** PLAYLIST ERROR")
            }
        }
    }
}
