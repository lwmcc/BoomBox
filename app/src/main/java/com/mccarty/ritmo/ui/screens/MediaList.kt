package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.domain.model.TrackDetails
import com.mccarty.ritmo.domain.playlists.PlaylistSelectAction
import com.mccarty.ritmo.ui.ItemColor
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.domain.tracks.TrackSelectAction


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaList(
    trackUri: String?,
    playListItem: Playlist?,
    tracks: List<TrackDetails>,
    onViewMoreClick: (Boolean, Int, List<TrackDetails>) -> Unit,
    onDetailsPlayPauseClicked: (TrackSelectAction) -> Unit,
    onPlaylistSelectAction: (PlaylistSelectAction) -> Unit,
    playlistId: String?,
    ) {

    Column {
        tracks.forEachIndexed { index, track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            onDetailsPlayPauseClicked(
                                TrackSelectAction.TrackSelect(
                                    index = index,
                                    uri = tracks[index].uri,
                                    duration = tracks[index].track?.duration_ms ?: 0L,
                                    tracks = tracks,
                                    playlistName = PlaylistNames.USER_PLAYLIST,
                                ),
                            )
                            onPlaylistSelectAction(PlaylistSelectAction.PlaylistSelect(playlistId))
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
                    val imageUrl = track.images.firstOrNull()?.url
                    GlideImage(
                        model = imageUrl,
                        contentDescription = stringResource(id = R.string.description_for_image),
                        modifier = Modifier.size(100.dp)
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .weight(1f),

                        ) {
                        Text(
                            text = track.trackName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth(),
                            color = ItemColor.currentItemColor().textColor(
                                playlist = playListItem,
                                mainItem = track,
                                trackUri = trackUri,
                                primary = MaterialTheme.colorScheme.primary,
                                onBackground = MaterialTheme.colorScheme.onBackground,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = track.albumName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth(),
                            color = ItemColor.currentItemColor().textColor(
                                playlist = playListItem,
                                mainItem = track,
                                trackUri = trackUri,
                                primary = MaterialTheme.colorScheme.primary,
                                onBackground = MaterialTheme.colorScheme.onBackground,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (track.artists.isNotEmpty()) {
                            Text(
                                text = track.artists[0].name ?: stringResource(id = R.string.track_name),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .paddingFromBaseline(top = 25.dp)
                                    .fillMaxWidth(),
                                color = ItemColor.currentItemColor().textColor(
                                    playlist = playListItem,
                                    mainItem = track,
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
                            onViewMoreClick(true, index, tracks)
                        }
                    )
                }
            }
        }
    }
}
