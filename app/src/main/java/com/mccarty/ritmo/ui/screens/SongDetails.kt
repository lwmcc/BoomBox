package com.mccarty.ritmo.ui.screens

import android.graphics.drawable.Icon
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.ui.graphics.Color
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.ui.MainImageHeader
import com.mccarty.ritmo.viewmodel.PlayerAction

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun SongDetailsScreen(
    model: MainViewModel,
    index: Int,
) {
    val recentlyPlayedMusic by model.recentlyPlayedMusic.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when(recentlyPlayedMusic) {
            is MainViewModel.RecentlyPlayedMusicState.Pending -> {
                println("MainScreen ***** PENDING")
            }

            is MainViewModel.RecentlyPlayedMusicState.Success<*> -> {
                val tracks = ((recentlyPlayedMusic as
                        MainViewModel.RecentlyPlayedMusicState.Success<*>).data.items)//.distinctBy { it.track.id }
                val pagerState = rememberPagerState(pageCount = { tracks.size })
                MediaDetails(pagerState, tracks, index, model)
            }

            else -> {
                println("MainScreen ***** ERROR")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun MediaDetails(
    pagerState: PagerState,
    items: List<Item>,
    index: Int,
    model: MainViewModel,
    ) {
    VerticalPager(state = pagerState) { page ->
        val image = items[page].track.album.images[0].url
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
                    text = "${items[page].track.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clickable {

                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayArrow, // TODO: will have to change with state
                        contentDescription = "play or pause",
                        modifier = Modifier.size(30.dp)
                    )
                }
                /*
                Button(
                    onClick = {

                    },
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        Icons.Default.PlayArrow, // TODO: will have to change with state
                        contentDescription = "play or pause",
                        modifier = Modifier.size(40.dp)
                    )
                }*/
            }
            Text(
                text = "${items[page].track.album.name}",
                style = MaterialTheme.typography.titleLarge
            )
            items[page].track.artists.forEach { artist ->
                Text(
                    text = "${artist.name}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text("${items[page].track.explicit}")
        }

        LaunchedEffect(key1 = 1) {
            pagerState.scrollToPage(index)
/*            snapshotFlow { pagerState.currentPage }.collect { page ->
                model.setArtistName(items[page].track.artists.firstOrNull()?.name)
            }*/
        }

        LaunchedEffect(key1 = 2) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                model.setArtistName(items[page].track.artists.firstOrNull()?.name)
            }
        }
    }
}