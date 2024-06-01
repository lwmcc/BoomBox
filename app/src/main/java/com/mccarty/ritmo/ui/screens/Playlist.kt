package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@Composable
fun PlaylistScreen(
    model: MainViewModel,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
) {
    val playLists by model.playLists.collectAsStateWithLifecycle()

    when(playLists) {
        is MainViewModel.PlaylistState.Pending -> {
            CircleSpinner(32.dp)
        }
        is MainViewModel.PlaylistState.Success -> {
            val tracks = (playLists as MainViewModel.PlaylistState.Success).trackDetails
            LazyColumn {
                item {
                    MediaList(
                        tracks,
                        onTrackClick = { index, tracks ->
                            // TODO: play track
                        },
                        onViewMoreClick = { showBottom, index, tracks ->
                            model.setPlayList(tracks)
                            onViewMoreClick(showBottom, index)
                        },
                        onAction =  {
                            onAction(it)
                        }
                    )
                }
            }

        } else -> {
            println("PLAYLIST ELSE")
        }
    }


}
