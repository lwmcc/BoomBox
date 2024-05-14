package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.MainActivity
import com.mccarty.ritmo.MainViewModel
import com.mccarty.ritmo.model.payload.Item

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun SongDetailsScreen(
    model: MainViewModel,
    trackId: String?,
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
                val tracks = ((recentlyPlayedMusic as MainViewModel.RecentlyPlayedMusicState.Success<*>).data.items)
                val pagerState = rememberPagerState(pageCount = { tracks.size })
                MediaDetails(pagerState, tracks)
            }

            else -> {
                println("MainScreen ***** ERROR")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun MediaDetails(pagerState: PagerState, items: List<Item>) {

    VerticalPager(state = pagerState) { page ->

        GlideImage(
            model = items[page].track.album.images[0].url,
            contentDescription = "",
            modifier = Modifier.size(300.dp),
        )
        Text("${items[page].track.album.images[0].url}")
        Text("${items[page].track.name}")
        Text("${items[page].track.album.name}")
        Text("${items[page].track.id}")
        Text("${items[page].track.artists}")
        Text("${items[page].track.explicit}")
        Text("${items[page].track.duration_ms}")

    }
}