package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistScreen(model: MainViewModel, navController: NavHostController) {
    val playlist by model.playlist.collectAsStateWithLifecycle()

    when(playlist) {
        is MainViewModel.PlaylistState.Pending-> {
            println("PlaylistScreen ***** PENDING")
        }

        is MainViewModel.PlaylistState.Success -> {
            MediaPlayList((playlist as MainViewModel.PlaylistState.Success).data) { playListItem, index ->
                navController.navigate("${MainActivity.SONG_DETAILS_KEY}${index}")
            }
        } else -> {
            println("PLAYLIST ELSE")
        }
    }


}
