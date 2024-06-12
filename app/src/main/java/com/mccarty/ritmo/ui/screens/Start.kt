package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mccarty.ritmo.MainActivity.Companion.INDEX_KEY
import com.mccarty.ritmo.MainActivity.Companion.MAIN_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_NAME_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.SONG_DETAILS_KEY
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@Composable
fun StartScreen(
    navController: NavHostController,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
    music: State<MainViewModel.MainItemsState>,
) {
    val mainViewModel: MainViewModel = viewModel()
    val details by mainViewModel.mediaDetails.collectAsStateWithLifecycle()
    val isPaused by mainViewModel.isPaused.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = MAIN_SCREEN_KEY) {
        composable(MAIN_SCREEN_KEY) {
            MainScreen(
                model = mainViewModel,
                music = music,
                navController = navController,
                onViewMoreClick = { showBottom, index, tracks ->
                    onViewMoreClick(showBottom, index)
                },
                onAction = {
                    onAction(it)
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
                index = backStackEntry.arguments?.getString(INDEX_KEY)?.toInt() ?: 0,
                onPlayPauseClicked = {
                    onPlayPauseClicked(it)
                }
            )
        }
        composable(
            "$PLAYLIST_SCREEN_KEY{$PLAYLIST_NAME_KEY}",
        ) { backStackEntry ->
            PlaylistScreen(
                title = backStackEntry.arguments?.getString(PLAYLIST_NAME_KEY),
                model = mainViewModel,
                onViewMoreClick = { showBottomSheet, index ->
                    onViewMoreClick(showBottomSheet, index)
                },
                onAction = {
                    onAction(it)
                }
            )
        }
    }
}