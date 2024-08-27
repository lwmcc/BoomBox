package com.mccarty.ritmo.ui.screens

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.playlists.PlaylistSelectAction
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.viewmodel.PlayerControlAction

@Composable
fun StartScreen(
    navController: NavHostController,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onDetailsPlayPauseClicked: (TrackSelectAction) -> Unit,
    onPlaylistSelectAction: (PlaylistSelectAction) -> Unit,
    onNavigateToPlaylist: (String?, String?) -> Unit,
    onPlayerControlAction: (PlayerControlAction) -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    mainItems: MainViewModel.MainItemsState,
    trackUri: String?,
    playlistId: String?,
    isPlaying: Boolean = false,
    @StringRes detailsTitle: Int,
) {
    val mainViewModel: MainViewModel = viewModel()
    val details by mainViewModel.mediaDetails.collectAsStateWithLifecycle()
    val isPaused by mainViewModel.isPaused.collectAsStateWithLifecycle(false)

    NavHost(navController = navController, startDestination = MAIN_SCREEN_KEY) {
        composable(MAIN_SCREEN_KEY) {
            MainScreen(
                model = mainViewModel, // TODO: move view model
                mainItems = mainItems,
                trackUri = trackUri,
                playlistId = playlistId,
                isPlaying = isPlaying,
                mainTitle = R.string.app_name,
                onViewMoreClick = { showBottom, index, _->
                    onViewMoreClick(showBottom, index)
                },
                onDetailsPlayPauseClicked = {
                    onDetailsPlayPauseClicked(it)
                },
                onNavigateToPlaylist = { name, id ->
                    onNavigateToPlaylist(name, id)
                },
                onPlayerControlAction = {
                    onPlayerControlAction(it)
                },
                onNavigateToDetails = {
                    onNavigateToDetails(it)
                }
            )
        }
        composable(
            "$SONG_DETAILS_KEY{$INDEX_KEY}",
        ) { backStackEntry ->
            SongDetailsScreen(
                isPaused = isPaused,
                details = details,
                model = mainViewModel,
                title = detailsTitle,
                index = backStackEntry.arguments?.getString(INDEX_KEY)?.toInt() ?: 0,
                onDetailsPlayPauseClicked = {
                    onDetailsPlayPauseClicked(it)
                },
                onPlayerControlAction = {
                    onPlayerControlAction(it)
                },
                onBack = {
                    navController.popBackStack()
                },
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
                onDetailsPlayPauseClicked = {
                    onDetailsPlayPauseClicked(it)
                },
                onPlaylistSelectAction = {
                    onPlaylistSelectAction(it)
                },
                onBack = {
                    navController.popBackStack()
                },
                onPLayerControlAction = {
                    onPlayerControlAction(it)
                },
                onNavigateToDetails = {
                    onNavigateToDetails(it)
                }
            )
        }
    }
}