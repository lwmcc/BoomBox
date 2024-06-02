package com.mccarty.ritmo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.ui.MainImageHeader
import com.mccarty.ritmo.ui.playPauseIcon
import com.mccarty.ritmo.viewmodel.TrackSelectAction

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun SongDetailsScreen(
    model: MainViewModel,
    index: Int,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
) {
    val tracks by model.playlistTracks.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val pagerState = rememberPagerState(pageCount = { tracks.size })
        MediaDetails(
            pagerState,
            tracks,
            index,
            model,
            onPlayPauseClicked = {
                onPlayPauseClicked(it)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun MediaDetails(
    pagerState: PagerState,
    tracks: List<TrackDetails>,
    index: Int,
    model: MainViewModel,
    onPlayPauseClicked: (TrackSelectAction) -> Unit,
    ) {
    val uri = model.trackUri.collectAsStateWithLifecycle()
    VerticalPager(state = pagerState) { page ->
        val image = tracks[page].images[0].url
        if (image.isNotEmpty()) {
            MainImageHeader(
                image,
                400.dp,
                50.dp,
                50.dp,
                Modifier,
            )
        }

        Spacer(modifier = Modifier.height(200.dp) )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text(
                    text = "${tracks[page].trackName}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clickable {
                        onPlayPauseClicked(TrackSelectAction.PlayTrackWithUri(tracks[page].uri))
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    if (uri.value == tracks[page].uri) {
                        playPauseIcon(painterResource(R.drawable.pause))
                    } else {
                        playPauseIcon(Icons.Default.PlayArrow)
                    }
                }
            }
            Text(
                text = "${tracks[page].albumName}",
                style = MaterialTheme.typography.titleLarge
            )
            tracks[page].artists.forEach { artist ->
                Text(
                    text = "${artist.name}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text("${tracks[page].explicit}")
        }

        LaunchedEffect(key1 = index) {
            pagerState.scrollToPage(index)
        }

        LaunchedEffect(key1 = 2) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                // TODO: don't pass in model
                //model.setArtistName(tracks[page].artists.firstOrNull()?.name)
            }
        }
    }
}