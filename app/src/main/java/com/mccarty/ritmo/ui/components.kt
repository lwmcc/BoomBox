package com.mccarty.ritmo.ui

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.payload.PlaylistData.Item as Item
import com.mccarty.ritmo.viewmodel.PlayerControlAction

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
        contentDescription = "", // TODO: add description
        modifier = modifier
            .size(imageSize)
            .padding(
                top = topPadding,
                bottom = bottomPadding,
            ),
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControls(
    mainViewModel: MainViewModel = viewModel(),
    onSlide: (PlayerControlAction) -> Unit,
    ) {
    val isPaused = mainViewModel.isPaused.collectAsStateWithLifecycle(false).value
    val position = mainViewModel.playbackPosition.collectAsStateWithLifecycle().value
    val duration = mainViewModel.playbackDuration.collectAsStateWithLifecycle().value.toFloat()

    var sliderPosition by remember { mutableFloatStateOf(position) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
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
                onSlide(PlayerControlAction.Seek(value))
            },
            interactionSource = interactionSource,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,

            ) {
            Button(
                onClick = {
                    onSlide(PlayerControlAction.Back)
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
                    onSlide(PlayerControlAction.Play(pausedPosition = position.toLong()))
                },
                contentPadding = PaddingValues(1.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                if (isPaused) {
                    playPauseIcon(playPause = R.drawable.play)
                } else {
                    playPauseIcon(playPause = R.drawable.pause)
                }
            }

            Button(
                onClick = {
                    onSlide(PlayerControlAction.Skip)
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
fun playPauseIcon(@DrawableRes playPause: Int) {
    Icon(
        painter = painterResource(playPause),
        contentDescription = "play or pause",
        modifier = Modifier.size(60.dp)
    )
}

@Composable
fun playPauseIcon(playPause: Painter) {
    Icon(
        painter = playPause,
        contentDescription = "play or pause",
        modifier = Modifier.size(60.dp)
    )
}

@Composable
fun playPauseIcon(playPause: ImageVector) {
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
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayList(
    playlist: List<Item>,
    onClick: (Int) -> Unit,
) {
    if (playlist.isNotEmpty()) {
        Text(
            text = stringResource(id = R.string.playlists),
            color = MaterialTheme.colorScheme.primary,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .paddingFromBaseline(top = 40.dp)
                .fillMaxWidth(),
        )
    }

    playlist.forEachIndexed { index, item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .clickable(onClick = {
                    onClick(index)
                }),
            shape = MaterialTheme.shapes.extraSmall,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            val imageUrl = item.images.firstOrNull()?.url
            Row {
                GlideImage(
                    model = imageUrl,
                    contentDescription = "",
                    modifier = Modifier
                        .size(100.dp),
                )

                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth(),
                    )
                    if (item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth(),
                        )
                    }
                    Text(
                        text = "${stringResource(R.string.total_tracks)} ${item.tracks.total}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}




