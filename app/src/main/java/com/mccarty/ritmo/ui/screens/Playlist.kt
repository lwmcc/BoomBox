package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainViewModel

@Composable
fun PlaylistScreen(
    model: MainViewModel,
    playlistId: String,
) {
    model.fetchPlaylist(playlistId)
    val playlist by model.playlist.collectAsStateWithLifecycle()

    when(playlist) {
        is MainViewModel.PlaylistState.Pending-> {
            println("PlaylistScreen ***** PENDING")
        }

        is MainViewModel.PlaylistState.Success -> {
            val playlist = (playlist as MainViewModel.PlaylistState.Success).playList


            playlist.forEach {
                println("***** 4${it.toString()}")
            }

/*            MediaPlayList(playlist) { item, index ->
                println("***** CLICK ${item.toString()}")
            }*/
        }

        else -> {
            println("PlaylistScreen ***** ERROR")
        }
    }


}
