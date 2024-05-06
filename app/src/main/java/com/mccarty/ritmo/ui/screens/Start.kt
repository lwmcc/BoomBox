package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.MainActivity.Companion.MAIN_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.SONG_DETAILS_KEY
import com.mccarty.ritmo.MainActivity.Companion.TRACK_ID_KEY
import com.mccarty.ritmo.MainViewModel

@Composable
fun StartScreen() {
    val mainViewModel: MainViewModel = viewModel()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MAIN_SCREEN_KEY) {
        composable(MAIN_SCREEN_KEY) {
            MainScreen(model = mainViewModel, navController = navController)
        }
        composable(
            "$SONG_DETAILS_KEY{$TRACK_ID_KEY}",
        ) { backStackEntry ->
            SongDetailsScreen(
                model = mainViewModel,
                trackId = backStackEntry.arguments?.getString(TRACK_ID_KEY),
            )
        }
        composable(PLAYLIST_SCREEN_KEY) {
            PlaylistScreen()
        }
    }
}