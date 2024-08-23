package com.mccarty.ritmo.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource as R
import androidx.compose.material3.IconButton as IconButton
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import com.mccarty.ritmo.viewmodel.PlaylistNames

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainImageHeader(
    imageUrl: String,
    imageSize: Dp,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    modifier: Modifier,
) {
    GlideImage(
        model = imageUrl,
        contentDescription = R(R.string.description_for_image),
        modifier = modifier
            .size(imageSize)
            .padding(
                top = topPadding,
                bottom = bottomPadding,
            ),
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun PlayerControls(
    mainViewModel: MainViewModel = viewModel(),
    onAction: (PlayerControlAction) -> Unit,
) {
    val isPaused = mainViewModel.isPaused.collectAsStateWithLifecycle(false).value
    val position = mainViewModel.playbackPosition.collectAsStateWithLifecycle(0).value.toFloat()
    val duration = mainViewModel.playbackDuration.collectAsStateWithLifecycle().value.toFloat()
    val playlist = mainViewModel.playlistData.collectAsStateWithLifecycle().value?.name?.name

    var sliderPosition by remember { mutableFloatStateOf(position) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val index by mainViewModel.playlistData.collectAsStateWithLifecycle()
    val isInteracting = isPressed || isDragged

    val value by derivedStateOf {
        if (isInteracting) {
            sliderPosition
        } else {
            position
        }
    }

    Column {
        Slider(
            value = value,
            onValueChange = {
                sliderPosition = it
            },
            valueRange = 0f..duration,
            steps = 1_000,

            onValueChangeFinished = {
                onAction(PlayerControlAction.Seek(value))
            },
            interactionSource = interactionSource,
        )

        Text(getPlaylist(playlist, LocalContext.current))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,

            ) {
            Button(
                onClick = {
                    onAction(PlayerControlAction.Back)
                },
                contentPadding = PaddingValues(1.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier.size(40.dp)
                )
            }

            Button(
                onClick = {
                    onAction(PlayerControlAction.Play(pausedPosition = position.toLong()))
                },
                contentPadding = PaddingValues(1.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                if (isPaused) {
                    PlayPauseIcon(playPause = R.drawable.play)
                } else {
                    PlayPauseIcon(playPause = R.drawable.pause)
                }
            }

            Button(
                onClick = {
                    onAction(PlayerControlAction.Skip(index?.index ?: 0))
                },
                contentPadding = PaddingValues(1.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.next),
                    contentDescription = "Skip Track",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun PlayPauseIcon(@DrawableRes playPause: Int) {
    Icon(
        painter = painterResource(playPause),
        contentDescription = "play or pause",
        modifier = Modifier.size(60.dp)
    )
}

@Composable
fun PlayPauseIcon(playPause: Painter) {
    Icon(
        painter = playPause,
        contentDescription = "play or pause",
        modifier = Modifier.size(60.dp)
    )
}

@Composable
fun PlayPauseIcon(playPause: ImageVector) {
    Icon(
        imageVector = playPause,
        contentDescription = "play or pause",
        modifier = Modifier.size(60.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showBottomSheet: Boolean,
    sheetState: SheetState,
    text: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss()
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(
                        start = 48.dp,
                        top = 8.dp,
                        bottom = 48.dp,
                    )
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = text,
                        modifier = Modifier
                            .clickable {
                                onClick()
                            }
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CircleSpinner(width: Dp = 64.dp) {
    CircularProgressIndicator(
        modifier = Modifier.width(width),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.secondary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenTopBar(@StringRes title: Int) {
    TopAppBar(
        title = { Text(text = R(id = title)) },
        navigationIcon = {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bb_home),
                    contentDescription = "Localized description"
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsTopBar(@StringRes title: Int, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = R(id = title)) },
        navigationIcon = {
            IconButton(onClick = {
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = R(R.string.menu_back),
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = R(R.string.menu_back),
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

fun getPlaylist(playlist: String?, context: Context): String {
    if (playlist.isNullOrEmpty()) {
        return context.getString(R.string.playing_from_recommended)
    }
    
    return when(playlist) {
        PlaylistNames.RECENTLY_PLAYED.name -> {
            context.getString(R.string.playing_from_recents)
        }
        PlaylistNames.RECOMMENDED_PLAYLIST.name -> {
            context.getString(R.string.playing_from_recommended)
        }
        PlaylistNames.USER_PLAYLIST.name-> {
            context.getString(R.string.playing_from_playlist)
        }
        else -> {
            context.getString(R.string.playing_from_recommended)
        }
    }
}