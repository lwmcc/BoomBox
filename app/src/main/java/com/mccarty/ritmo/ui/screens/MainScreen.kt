package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.MainActivity
import com.bumptech.glide.integration.compose.GlideImage as GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.ui.ItemColor
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun MainScreen(
    model: MainViewModel,
    onViewMoreClick: (Boolean, Int, List<MainItem>) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    navController: NavHostController = rememberNavController(),
    music: State<MainViewModel.MainItemsState>,
    trackUri: State<String?>,
) {
    val musicHeader by model.musicHeader.collectAsStateWithLifecycle()
    val mainMusic by model.mainItems.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tracksHeader = context.getString(R.string.recently_played)
    val playlistsHeader = context.getString(R.string.playlists)
    val playListItem by model.playlistData.collectAsStateWithLifecycle()

    val timeOut by remember { mutableLongStateOf(10_000L) }

    when (music.value) {
        is MainViewModel.MainItemsState.Pending -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val noInternet =  stringResource(id = R.string.no_internet_connection)
                CircleSpinner(32.dp)
                LaunchedEffect(Unit) {
                    delay(timeOut)
                    model.setMainItemsError(noInternet)
                }
            }
        }

        is MainViewModel.MainItemsState.Success -> {
            val mainItems = (mainMusic as MainViewModel.MainItemsState.Success).mainItems
            val tracks = mainItems.map {
                Group(
                    type = it.key,
                    items = it.value,
                )
            }

            /**
             * Section holding header, recently played, and playlist
             */
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                tracks.forEachIndexed { index, group ->
                    stickyHeader {
                        /**
                         * Section header, ie Recently Played and Playlist
                         */
                        Column(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.background),
                        ) {
                            /**
                             * Section header text Recently Played and Playlist
                             */
                            Text(
                                text = stickyHeaderText(group.type, tracksHeader, playlistsHeader),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .paddingFromBaseline(top = 40.dp)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            )
                        }
                    }

                    if (index == 0) {
                        item {
                            MainHeader(
                                imageUrl = musicHeader.imageUrl.toString(),
                                artistName = musicHeader.artistName,
                                albumName = musicHeader.albumName,
                                songName = musicHeader.songName,
                                modifier = Modifier,
                            )
                        }
                    }

                    itemsIndexed(group.items) { itemIndex, item ->
                        when (item.type) {
                            CollectionType.TRACK.collectionType -> {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            onClick = {
                                                onAction(
                                                    TrackSelectAction.TrackSelect(
                                                        index = itemIndex,
                                                        duration = group.items[itemIndex].track?.duration_ms
                                                            ?: 0L, // TODO: fix
                                                        uri = group.items[itemIndex].track?.uri
                                                            ?: "",
                                                        tracks = group.items,
                                                        playlistName = PlaylistNames.RECENTLY_PLAYED,
                                                    )
                                                )
                                            }
                                        )
                                        .padding(5.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RectangleShape,
                                            clip = false,
                                            ambientColor = DefaultShadowColor,
                                            spotColor = DefaultShadowColor,
                                        ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                    ),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val imageUrl = item.track?.album?.images?.firstOrNull()?.url
                                        GlideImage(
                                            model = imageUrl,
                                            contentDescription = stringResource(R.string.description_for_image),
                                            modifier = Modifier.size(100.dp)
                                        )

                                        Column(
                                            modifier = Modifier
                                                .padding(start = 20.dp)
                                                .weight(1f),

                                            ) {

                                            Text(
                                                text = item.track?.name.toString(),
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier
                                                    .paddingFromBaseline(top = 25.dp)
                                                    .fillMaxWidth(),
                                                color = ItemColor.currentItemColor().textColor(playListItem, item, trackUri.value),
                                            )
                                            Text(
                                                text = item.track?.album?.name ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .paddingFromBaseline(top = 25.dp)
                                                    .fillMaxWidth()
                                            )
                                            if (item.track?.artists?.isNotEmpty() == true) {
                                                Text(
                                                    text = item.track?.artists?.get(0)?.name ?: androidx.ui.res.stringResource(R.string.track_name),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier
                                                        .paddingFromBaseline(top = 25.dp)
                                                        .fillMaxWidth()
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(
                                                id = R.string.icon_view_more,
                                            ),
                                            modifier = Modifier.clickable {
                                                model.setPlayList(group.items)
                                                onViewMoreClick(true, itemIndex, group.items)
                                            }
                                        )
                                    }
                                }
                            }

                           CollectionType.PLAYLIST.collectionType -> {
                               Card(
                                   modifier = Modifier
                                       .fillMaxWidth()
                                       .padding(5.dp)
                                       .clickable(onClick = {
                                           model.fetchPlaylist(item.id ?: "")
                                           navController.navigate(
                                               "${MainActivity.PLAYLIST_SCREEN_KEY}${item.name}"
                                           )
                                       })
                                       .shadow(
                                           elevation = 2.dp,
                                           shape = RectangleShape,
                                           clip = false,
                                           ambientColor = DefaultShadowColor,
                                           spotColor = DefaultShadowColor,
                                       ),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
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
                                                text = item.name.toString(),
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier
                                                    .paddingFromBaseline(top = 25.dp)
                                                    .fillMaxWidth(),
                                            )
                                            if (item.description?.isNotEmpty() == true) {
                                                Text(
                                                    text = item.description
                                                        ?: "", // TODO: use some text
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier
                                                        .paddingFromBaseline(top = 25.dp)
                                                        .fillMaxWidth(),
                                                )
                                            }
                                            Text(
                                                text = "${stringResource(R.string.total_tracks)} ${item.tracks?.total}",
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
                    }
                }
            }
        }

        is MainViewModel.MainItemsState.Error<*> -> {
            val error = (mainMusic as MainViewModel.MainItemsState.Error<*>).message.toString()
            ErrorScreen(error)
        }
    }
}

fun stickyHeaderText(type: String, tracks: String, playlists: String): String {
    return when (type) {
        CollectionType.TRACK.collectionType -> tracks
        CollectionType.PLAYLIST.collectionType -> playlists
        else -> {
            tracks
        }
    }
}

data class Group(
    val type: String,
    val items: List<MainItem>
)

enum class CollectionType(val collectionType: String) {
    TRACK("track"), PLAYLIST("playlist")
}