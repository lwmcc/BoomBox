package com.mccarty.ritmo.ui.navigation

import androidx.navigation.NavHostController
import com.mccarty.ritmo.MainActivity

class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToPlaylist(name: String, id: String) {
        navController.navigate(
            route = "${MainActivity.PLAYLIST_SCREEN_KEY}/${name}/${id}",
        )
    }

    fun navigateToTrackDetails(trackIndex: Int) {
        navController.navigate("${MainActivity.SONG_DETAILS_KEY}${trackIndex}")
    }
}