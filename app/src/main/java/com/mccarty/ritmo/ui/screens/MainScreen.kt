package com.mccarty.ritmo.ui.screens

import androidx.annotation.StringRes
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage as GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.viewmodel.MainViewModel
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.ui.CircleSpinner
import com.mccarty.ritmo.ui.ItemColor
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.domain.tracks.TrackSelectAction
import com.mccarty.ritmo.ui.MainScreenTopBar
import com.mccarty.ritmo.ui.PlayerControls
import com.mccarty.ritmo.viewmodel.PlayerControlAction
import kotlinx.coroutines.delay
import com.mccarty.ritmo.viewmodel.MainViewModel.MainItemsState as MainItemsState

@OptIn(
    ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    onViewMoreClick: (Boolean, Int, List<MainItem>) -> Unit,
    onDetailsPlayPauseClicked: (TrackSelectAction) -> Unit,
    onNavigateToPlaylist: (String?, String?) -> Unit,
    onPlayerControlAction: (PlayerControlAction) -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    mainItemsState: MainItemsState,
    trackUri: String?,
    playlistId: String?,
    isPlaying: Boolean = false,
    @StringRes mainTitle: Int,
) {
    val musicHeader by mainViewModel.musicHeader.collectAsStateWithLifecycle()
    val mainMusic by mainViewModel.mainItems.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tracksHeader = context.getString(R.string.recently_played)
    val playlistsHeader = context.getString(R.string.playlists)
    val playListItem by mainViewModel.playlistData.collectAsStateWithLifecycle()

    val timeOut by remember { mutableLongStateOf(5_000) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { MainScreenTopBar(mainTitle) },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                PlayerControls(
                    mainViewModel = mainViewModel,
                    onPlayerControlAction = { onPlayerControlAction(it) },
                    onShowDetailsAction = {
                        playListItem?.tracks?.let { mainViewModel.setPlayList(it) }
                        onNavigateToDetails(playListItem?.index ?: 0)
                    },
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (mainItemsState) {
                is MainItemsState.Pending -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircleSpinner(32.dp)
                        LaunchedEffect(Unit) {
                            delay(timeOut)
                            // Sometimes there is a lag when fetching from the API
                            // Fetch again here if this timeout is reached
                            mainViewModel.fetchMainMusic()
                        }
                    }
                }

                is MainItemsState.Success -> {
                    val tracks = (mainMusic as MainItemsState.Success).mainItems.map {
                        Group(
                            type = it.key,
                            items = it.value,
                        )
                    }
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
                                    /**
                                     * Track shown in Recently Played list
                                     */
                                    CollectionType.TRACK.collectionType -> {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    onClick = {
                                                        onDetailsPlayPauseClicked(// TODO: this name is used in details
                                                                                  // rename it to something that can be used
                                                                                  // in details in in the list if items
                                                            TrackSelectAction.TrackSelect(
                                                                index = itemIndex,
                                                                duration = group.items[itemIndex].track?.duration_ms
                                                                    ?: 0L,
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
                                                        color = ItemColor.currentItemColor().textColor(
                                                            playlist = playListItem,
                                                            mainItem = item,
                                                            trackUri = trackUri,
                                                            primary = MaterialTheme.colorScheme.primary,
                                                            onBackground = MaterialTheme.colorScheme.onBackground,
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    Text(
                                                        text = item.track?.album?.name ?: "",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier
                                                            .paddingFromBaseline(top = 25.dp)
                                                            .fillMaxWidth(),
                                                        color = ItemColor.currentItemColor().textColor(
                                                            playlist = playListItem,
                                                            mainItem = item,
                                                            trackUri = trackUri,
                                                            primary = MaterialTheme.colorScheme.primary,
                                                            onBackground = MaterialTheme.colorScheme.onBackground,
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    if (item.track?.artists?.isNotEmpty() == true) {
                                                        Text(
                                                            text = item.track?.artists?.get(0)?.name
                                                                ?: androidx.ui.res.stringResource(R.string.track_name),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            modifier = Modifier
                                                                .paddingFromBaseline(top = 25.dp)
                                                                .fillMaxWidth(),
                                                            color = ItemColor.currentItemColor().textColor(
                                                                playlist = playListItem,
                                                                mainItem = item,
                                                                trackUri = trackUri,
                                                                primary = MaterialTheme.colorScheme.primary,
                                                                onBackground = MaterialTheme.colorScheme.onBackground,
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    Icons.Default.MoreVert,
                                                    contentDescription = stringResource(
                                                        id = R.string.icon_view_more,
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        mainViewModel.setPlayList(group.items)
                                                        onViewMoreClick(true, itemIndex, group.items)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    /**
                                     * Playlist name show in list fo playlists
                                     */
                                    CollectionType.PLAYLIST.collectionType -> {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(5.dp)
                                                .clickable(onClick = {
                                                    mainViewModel.fetchPlaylist(item.id)
                                                    onNavigateToPlaylist(item.name, item.id)
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
                                                        color = ItemColor.currentItemColor().textColor(
                                                            isPlaying = isPlaying,
                                                            primary = MaterialTheme.colorScheme.primary,
                                                            onBackground = MaterialTheme.colorScheme.onBackground,
                                                            playListItem?.name?.name,
                                                            PlaylistNames.USER_PLAYLIST.name,
                                                            playlistId,
                                                            item.id,
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    if (item.description?.isNotEmpty() == true) {
                                                        Text(
                                                            text = item.description
                                                                ?: "", // TODO: use some text
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            modifier = Modifier
                                                                .paddingFromBaseline(top = 25.dp)
                                                                .fillMaxWidth(),
                                                            color = ItemColor.currentItemColor().textColor(
                                                                isPlaying = isPlaying,
                                                                primary = MaterialTheme.colorScheme.primary,
                                                                onBackground = MaterialTheme.colorScheme.onBackground,
                                                                playListItem?.name?.name,
                                                                PlaylistNames.USER_PLAYLIST.name,
                                                                playlistId,
                                                                item.id,
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                    }
                                                    Text(
                                                        text = "${stringResource(R.string.total_tracks)} ${item.tracks?.total}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier
                                                            .paddingFromBaseline(top = 25.dp)
                                                            .fillMaxWidth(),
                                                        color = ItemColor.currentItemColor().textColor(
                                                            isPlaying = isPlaying,
                                                            primary = MaterialTheme.colorScheme.primary,
                                                            onBackground = MaterialTheme.colorScheme.onBackground,
                                                            playListItem?.name?.name,
                                                            PlaylistNames.USER_PLAYLIST.name,
                                                            playlistId,
                                                            item.id,
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
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

                is MainItemsState.Error<*> -> {
                    val error = (mainMusic as MainItemsState.Error<*>).message.toString()
                    ErrorScreen(error)
                }
            }
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