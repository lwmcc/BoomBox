package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.MainActivity.Companion.INDEX_KEY
import com.mccarty.ritmo.MainActivity.Companion.MAIN_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_ID_KEY
import com.mccarty.ritmo.MainActivity.Companion.PLAYLIST_SCREEN_KEY
import com.mccarty.ritmo.MainActivity.Companion.SONG_DETAILS_KEY
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
            "$SONG_DETAILS_KEY{$INDEX_KEY}",
        ) { backStackEntry ->
            SongDetailsScreen(
                model = mainViewModel,
                index = backStackEntry.arguments?.getString(INDEX_KEY)?.toInt() ?: 0,
            )
        }
        composable(
            "$PLAYLIST_SCREEN_KEY{$PLAYLIST_ID_KEY}",
            ) {  backStackEntry ->
            PlaylistScreen(
                model = mainViewModel,
                playlistId = backStackEntry.arguments?.getString(PLAYLIST_ID_KEY) ?: "",
                ) // TODO: null
        }
    }
}