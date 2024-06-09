package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@Composable
fun PlaylistScreen(
    model: MainViewModel,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
) {
    val playLists by model.playLists.collectAsStateWithLifecycle()

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
                        tracks,
                        onViewMoreClick = { showBottom, index, tracks ->
                            model.setPlayList(tracks)
                            onViewMoreClick(showBottom, index)
                        }
                    ) {
                        onAction(it)
                    }
                }
            }
        }

        else -> {
            println("PLAYLIST ELSE")
        }
    }
}