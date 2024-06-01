package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.TrackDetails

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistScreen(
    model: MainViewModel,
    onViewMoreClick: (Int, List<TrackDetails>) -> Unit,
    navController: NavHostController,
) {
    val playlist by model.playlist.collectAsStateWithLifecycle()
    val playLists by model.playLists.collectAsStateWithLifecycle()


    when(playLists) {
        is MainViewModel.PlaylistState.Pending -> {
            println("PlaylistScreen ***** PENDING")
        }

        is MainViewModel.PlaylistState.Success -> {
            // TODO: make collection of TrackDetails and use for details
            val tracks = (playLists as MainViewModel.PlaylistState.Success).trackDetails
            LazyColumn {
                item {
                    MediaList(
                        tracks,
                        onTrackClick = { index, tracks2 ->
                            model.setPlayList(tracks2)
                            navController.navigate("${MainActivity.SONG_DETAILS_KEY}${index}")
                        },
                        onViewMoreClick = { index, tracks ->
                            model.setPlayList(tracks)
                            onViewMoreClick(index, tracks)
                            //navController.navigate("${MainActivity.SONG_DETAILS_KEY}${index}")
                        }
                    )
                }
            }

        } else -> {
            println("PLAYLIST ELSE")
        }
    }


}
