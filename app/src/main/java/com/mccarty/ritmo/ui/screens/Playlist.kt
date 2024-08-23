package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.ui.res.stringResource
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.playlists.PlaylistSelectAction
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.ui.NavTopBar

@Composable
fun PlaylistScreen(
    title: String?,
    playlistId: String?,
    model: MainViewModel,
    modifier: Modifier = Modifier,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    onPlaylistSelectAction: (PlaylistSelectAction) -> Unit,
    onBack:() -> Unit,
) {
    val playLists by model.playLists.collectAsStateWithLifecycle()
    val playListItem by model.playlistData.collectAsStateWithLifecycle()
    val trackUri = model.trackUri.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavTopBar(title ?: stringResource(R.string.playlist)) {
                onBack()
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (playLists) {
                is MainViewModel.PlaylistState.Pending -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircleSpinner(32.dp)
                    }
                }

                is MainViewModel.PlaylistState.Success -> {
                    val tracks = (playLists as MainViewModel.PlaylistState.Success).trackDetails
                    LazyColumn {
                        item {
                            MediaList(
                                trackUri = trackUri,
                                playlistId = playlistId,
                                playListItem = playListItem,
                                tracks = tracks,
                                onViewMoreClick = { showBottom, index, tracks ->
                                    model.setPlayList(tracks)
                                    onViewMoreClick(showBottom, index)
                                },
                                onAction = {
                                    onAction(it)
                                },
                                onPlaylistSelectAction = {
                                    onPlaylistSelectAction(PlaylistSelectAction.PlaylistSelect(playlistId))
                                },
                            )
                        }
                    }
                }

                else -> {
                    println("PLAYLIST ELSE")
                }
            }
        }
    }
}