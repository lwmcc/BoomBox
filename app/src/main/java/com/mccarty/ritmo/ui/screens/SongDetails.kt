package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.ui.MainImageHeader

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
            Text(
                text = "${items[page].track.name}",
                style = MaterialTheme.typography.headlineMedium,
            )
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