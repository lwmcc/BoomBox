package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mccarty.ritmo.MainActivity.Companion.INDEX_KEY
import com.mccarty.ritmo.MainActivity.Companion.MAIN_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_ID_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_NAME_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.SONG_DETAILS_KEY
import com.mccarty.ritmo.domain.playlists.PlaylistSelectAction
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.domain.tracks.TrackSelectAction

@Composable
fun StartScreen(
    navController: NavHostController,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    onPlaylistSelectAction: (PlaylistSelectAction) -> Unit,
    music: State<MainViewModel.MainItemsState>,
    trackUri: State<String?>,
    playlistId: String?,
    isPlaying: Boolean = false,
) {
    val mainViewModel: MainViewModel = viewModel()
    val details by mainViewModel.mediaDetails.collectAsStateWithLifecycle()
    val isPaused by mainViewModel.isPaused.collectAsStateWithLifecycle(false)

    NavHost(navController = navController, startDestination = MAIN_SCREEN_KEY) {
        composable(MAIN_SCREEN_KEY) {
            MainScreen(
                model = mainViewModel, // TODO: move view model
                music = music,
                trackUri = trackUri,
                playlistId = playlistId,
                navController = navController,
                isPlaying = isPlaying,
                onViewMoreClick = { showBottom, index, _->
                    onViewMoreClick(showBottom, index)
                },
                onAction = {
                    onAction(it)
                },
            )
        }
        composable(
            "$SONG_DETAILS_KEY{$INDEX_KEY}",
        ) { backStackEntry ->
            SongDetailsScreen(
                isPaused = isPaused,
                details = details,
                model = mainViewModel,
                index = backStackEntry.arguments?.getString(INDEX_KEY)?.toInt() ?: 0,
                onPlayPauseClicked = {
                    onAction(it)
                }
            )
        }
        composable(
            route = "${PLAYLIST_SCREEN_KEY}/{$PLAYLIST_NAME_KEY}/{$PLAYLIST_ID_KEY}",
            arguments = listOf(
                navArgument(PLAYLIST_NAME_KEY) { type = NavType.StringType },
                navArgument(PLAYLIST_ID_KEY) { type = NavType.StringType },
            )
        ) { backStackEntry ->
            PlaylistScreen(
                title = backStackEntry.arguments?.getString(PLAYLIST_NAME_KEY),
                playlistId = backStackEntry.arguments?.getString(PLAYLIST_ID_KEY),
                model = mainViewModel,
                onViewMoreClick = { showBottomSheet, index ->
                    onViewMoreClick(showBottomSheet, index)
                },
                onAction = {
                    onAction(it)
                },
                onPlaylistSelectAction = {
                    onPlaylistSelectAction(it)
                }
            )
        }
    }
}