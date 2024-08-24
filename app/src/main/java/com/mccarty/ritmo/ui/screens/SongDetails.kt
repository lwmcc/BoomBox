package com.mccarty.ritmo.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.ui.res.stringResource
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.ui.MainImageHeader
import com.mccarty.ritmo.ui.PlayPauseIcon
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.ui.TrackDetailsTopBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongDetailsScreen(
    isPaused: Boolean,
    index: Int,
    model: MainViewModel,
    details: List<Details>,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
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
        Column(
            modifier = Modifier.padding(start = 24.dp, top = paddingValues.calculateTopPadding(), end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val pagerState = rememberPagerState(pageCount = { details.size })
            MediaDetails(
                isPaused = isPaused,
                pagerState,
                details,
                index,
                model,
                onPlayPauseClicked = {
                    onPlayPauseClicked(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaDetails(
    isPaused: Boolean,
    pagerState: PagerState,
    tracks: List<Details>,
    index: Int,
    model: MainViewModel,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
    ) {

    val uri = model.trackUri.collectAsStateWithLifecycle()
    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 2,
    ) { page ->
        Column(modifier = Modifier.fillMaxWidth()) {
            val image = tracks[page].images?.get(0)?.url
            if (image?.isNotEmpty() == true) {
                MainImageHeader(
                    image,
                    400.dp,
                    24.dp,
                    50.dp,
                    Modifier,
                )
            }

            Spacer(modifier = Modifier.height(100.dp) )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Text(
                        text = tracks[page].trackName.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Box(modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            onPlayPauseClicked(TrackSelectAction.PlayTrackWithUri(tracks[page].uri))
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!isPaused) {
                            if (uri.value == tracks[page].uri) {
                                PlayPauseIcon(painterResource(R.drawable.baseline_pause_24))
                            } else {
                                PlayPauseIcon(Icons.Default.PlayArrow)
                            }
                        } else {
                            PlayPauseIcon(Icons.Default.PlayArrow)
                        }
                    }
                }
                Text(
                    text = "${tracks[page].albumName}",
                    style = MaterialTheme.typography.titleLarge
                )
                tracks[page].artists?.forEach { artist ->
                    Text(
                        text = artist.name ?: stringResource(R.string.track_name),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }

                if (tracks[page].explicit) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.baseline_explicit_24),
                        contentDescription = androidx.compose.ui.res.stringResource(R.string.explicit_content),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            LaunchedEffect(key1 = tracks[page].uri) {
                pagerState.scrollToPage(index)
            }
        }
    }
}