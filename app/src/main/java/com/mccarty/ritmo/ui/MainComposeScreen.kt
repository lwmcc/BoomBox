package com.mccarty.ritmo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.playlists.PlaylistSelectAction
import com.mccarty.ritmo.ui.navigation.AppNavigationActions
import com.mccarty.ritmo.ui.screens.StartScreen
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposeScreen(
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController(),
    viewMore: String,
    padding: PaddingValues,
    mediaEvents: MainActivity.MediaEvents,
    onPlaylistSelectAction: (PlaylistSelectAction) -> Unit,
    onPlayerControlAction: (PlayerControlAction) -> Unit,
    onViewArtistClick: () -> Unit,
    navActions: AppNavigationActions = remember(navController) {
        AppNavigationActions(navController)
    }
) {

    val sheetState = rememberModalBottomSheetState()
    val mainItemsState = mainViewModel.mainItems.collectAsStateWithLifecycle().value
    var showBottomSheet by remember { mutableStateOf(false) }
    var trackIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val isPaused = mainViewModel.isPaused.collectAsStateWithLifecycle()
    val trackUri = mainViewModel.trackUri.collectAsStateWithLifecycle().value
    val playListId = mainViewModel.playlistId.collectAsStateWithLifecycle().value
    val isPlaying =!mainViewModel.isPaused.collectAsStateWithLifecycle().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                )
        ) {
            StartScreen (
                mainItemsState = mainItemsState,
                trackUri = trackUri,
                playlistId = playListId,
                navController = navController,
                isPlaying = isPlaying,
                detailsTitle =  R.string.top_bar_track_details,
                onViewMoreClick = { bottomSheet, index ->
                    showBottomSheet = bottomSheet
                    trackIndex = index
                },
                onDetailsPlayPauseClicked = {
                    mediaEvents.trackSelectionAction(it, isPaused) // TODO: send up another level
                },
                onPlaylistSelectAction = {
                    onPlaylistSelectAction(it)
                },
                onNavigateToPlaylist = { name, id ->
                    navActions.navigateToPlaylist(name, id)
                },
                onPlayerControlAction = {
                    onPlayerControlAction(it)
                },
                onNavigateToDetails = {
                    navActions.navigateToTrackDetails(it)
                },
            )
        }
    }

    BottomSheet(
        showBottomSheet,
        sheetState = sheetState,
        text = viewMore,
        onDismiss = {
            showSheet(
                scope = scope,
                sheetState = sheetState,
            ) {
                showBottomSheet = it
            }
        },
        onClick = {
            showSheet(
                scope = scope,
                sheetState = sheetState,
            ) {
                showBottomSheet = it
            }
            navActions.navigateToTrackDetails(trackIndex)
        },
        onViewArtistClick = {
            onViewArtistClick()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun showSheet(
    scope: CoroutineScope,
    sheetState: SheetState,
    onShowSheet: (Boolean) -> Unit,
) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) {
            onShowSheet(false)
        }
    }
}