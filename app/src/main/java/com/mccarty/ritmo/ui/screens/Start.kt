package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.MainActivity.Companion.INDEX_KEY
import com.mccarty.ritmo.MainActivity.Companion.MAIN_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_ID_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.SONG_DETAILS_KEY
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@Composable
fun StartScreen(
    navController: NavHostController,
    onViewMoreClick: (Boolean, Int) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
) {
    val mainViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = MAIN_SCREEN_KEY) {
        composable(MAIN_SCREEN_KEY) {
            MainScreen(
                model = mainViewModel,
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
                model = mainViewModel,
                index = backStackEntry.arguments?.getString(INDEX_KEY)?.toInt() ?: 0,
                onPlayPauseClicked = {
                    onPlayPauseClicked(it)
                }
            )
        }
        composable(
            "$PLAYLIST_SCREEN_KEY{$PLAYLIST_ID_KEY}",
        ) {
            PlaylistScreen(
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