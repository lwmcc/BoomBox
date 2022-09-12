package com.mccarty.ritmo.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.MainViewModel

// The screen shown on app start
@Composable
fun StartScreen(model : MainViewModel) { // TODO: inject model

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController = navController, model = model)
        }
        composable("song_details") {
            SongDetailsScreen()
        }
        composable("playlist_screen") {
            PlaylistScreen()
        }
    }
}