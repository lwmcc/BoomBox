package com.mccarty.ritmo.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.ui.res.stringResource
import androidx.compose.ui.res.stringResource as CR
import com.mccarty.ritmo.MainActivity.Companion.PAGER_SCROLL_DELAY
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.ui.MainImageHeader
import com.mccarty.ritmo.ui.PlayPauseIcon
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.ui.TrackDetailsTopBar
import com.mccarty.ritmo.utils.createListFromDetails
import com.mccarty.ritmo.utils.createStringFromCollection
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import kotlinx.coroutines.launch

/**
 * Composable for the Track Details Screen
 * Takes an array [trackDetails] and sets the first viewed details screen
 * according to [initialPagerIndex] which is the selected index from the list
 * of Tracks
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongDetailsScreen(
    isPaused: Boolean,
    initialPagerIndex: Int,
    mainViewModel: MainViewModel,
    trackDetails: List<Details>,
    onDetailsPlayPauseClicked: (TrackSelectAction) -> Unit,
    onPlayerControlAction: (PlayerControlAction) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    @StringRes title: Int,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TrackDetailsTopBar(title) {
                onBack()
            }
        }
    ) { paddingValues ->
        val recentTrackDetails = trackDetails.createListFromDetails(mainViewModel.recentlyPlayedMusic())

        Column(
            modifier = Modifier.padding(start = 24.dp, top = paddingValues.calculateTopPadding(), end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val pagerState = rememberPagerState(
                pageCount = { recentTrackDetails.size },
                initialPage = initialPagerIndex,
            )

            MediaDetails(
                isPaused = isPaused,
                pagerState = pagerState,
                trackDetails = recentTrackDetails,
                initialPagerIndex = initialPagerIndex,
                mainViewModel = mainViewModel,
                onDetailsPlayPauseClicked = {
                    onDetailsPlayPauseClicked(it)
                },
                onPlayerControlAction = {
                    onPlayerControlAction(it)
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaDetails(
    isPaused: Boolean,
    pagerState: PagerState,
    trackDetails: List<Details>,
    initialPagerIndex: Int,
    mainViewModel: MainViewModel,
    onDetailsPlayPauseClicked: (TrackSelectAction) -> Unit,
    onPlayerControlAction: (PlayerControlAction) -> Unit,
    ) {

    val position = mainViewModel.playbackPosition.collectAsStateWithLifecycle(0).value.toFloat()
    val duration = mainViewModel.playbackDuration.collectAsStateWithLifecycle().value.toFloat()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()

    val uri = mainViewModel.trackUri.collectAsStateWithLifecycle()

    var sliderPosition by remember { mutableFloatStateOf(position) }
    val isInteracting = isPressed || isDragged

    var rememberIndex by rememberSaveable { mutableIntStateOf(initialPagerIndex) }

    val scope = rememberCoroutineScope()

    val sliderValue by remember { // TODO: recheck if remember or rememberSaveable is better
        derivedStateOf {
            if (isInteracting) {
                sliderPosition
            } else {
                position
            }
        }
    }

    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 2,
    ) { page ->
        Column(modifier = Modifier.fillMaxWidth()) {
            val image = trackDetails[page].images?.get(0)?.url
            if (image?.isNotEmpty() == true) {
                MainImageHeader(
                    image,
                    450.dp,
                    24.dp,
                    48.dp,
                    Modifier,
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text(
                        text = trackDetails[page].trackName.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "${trackDetails[page].albumName}",
                    style = MaterialTheme.typography.titleLarge
                )

                trackDetails[page].artists?.createStringFromCollection(CR(R.string.track_name))
                    ?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                if (trackDetails[page].explicit) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_explicit_24),
                        contentDescription = CR(R.string.explicit_content),
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderPosition = it
                    },
                    valueRange = 0f..duration,
                    steps = 1_000,

                    onValueChangeFinished = {
                        onPlayerControlAction(PlayerControlAction.Seek(sliderValue))
                    },
                    interactionSource = interactionSource,
                )
                /**
                 * Previous, play/pause, and next buttons
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = rememberIndex - 1,
                                    animationSpec = tween(durationMillis = PAGER_SCROLL_DELAY),
                                )
                            }
                        },
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_skip_previous_24),
                            contentDescription = "Back",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                onDetailsPlayPauseClicked(
                                    TrackSelectAction.PlayTrackWithUri(
                                        trackDetails[page].uri
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!isPaused) {
                            if (uri.value == trackDetails[page].uri) {
                                PlayPauseIcon(painterResource(R.drawable.baseline_pause_24))
                            } else {
                                PlayPauseIcon(Icons.Default.PlayArrow)
                            }
                        } else {
                            PlayPauseIcon(Icons.Default.PlayArrow)
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = rememberIndex + 1,
                                    animationSpec = tween(durationMillis = PAGER_SCROLL_DELAY),
                                )
                            }
                        },
                        contentPadding = PaddingValues(1.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_skip_next_w_24),
                            contentDescription = "Skip Track",
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
            }

            LaunchedEffect(key1 = trackDetails[page].uri) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    if (rememberIndex != page) {
                        onDetailsPlayPauseClicked(TrackSelectAction.PlayTrackScrolledToWithUri(trackDetails[page].uri))
                    }
                    rememberIndex = page
                }
            }
        }
    }
}