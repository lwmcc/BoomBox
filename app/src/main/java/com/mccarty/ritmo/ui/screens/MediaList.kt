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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.viewmodel.PlaylistNames
import com.mccarty.ritmo.viewmodel.TrackSelectAction


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaList(
    title: String?,
    tracks: List<TrackDetails>,
    onViewMoreClick: (Boolean, Int, List<TrackDetails>) -> Unit,
    onAction: (TrackSelectAction) -> Unit,
    ) {

    if (tracks.isNotEmpty()) {
        Text(
            text = title ?: "${stringResource(R.string.playlist)}",
            color = MaterialTheme.colorScheme.primary,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .paddingFromBaseline(top = 40.dp)
                .fillMaxWidth()
                .padding(16.dp),
        )
    }

    Column {
        tracks.forEachIndexed { index, track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            // TODO: fix !! and ?:
                            onAction(
                                TrackSelectAction.TrackSelect(
                                    index = index,
                                    uri = tracks[index].uri!!,
                                    duration = tracks[index].track?.duration_ms ?: 0L,
                                    tracks = tracks,
                                    playlistName = PlaylistNames.RECENTLY_PLAYED,
                                )
                            )
                        }
                    )
                    .padding(5.dp),
                shape = MaterialTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imageUrl = track.images.firstOrNull()?.url
                    GlideImage(
                        model = imageUrl,
                        contentDescription = "", // TODO: add description
                        modifier = Modifier.size(100.dp)
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .weight(1f),

                        ) {
                        Text(

                            text = track.trackName, // TODO: fix !!
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth(),
                        )
                        Text(
                            text = track.albumName,  // TODO: fix !!
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth()
                        )

                        if (track.artists.isNotEmpty()) { // TODO; fix !!
                            Text(

                                text = track.artists[0].name, // TODO: fix !!
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
                            onViewMoreClick(true, index, tracks)
                        }
                    )
                }
            }
        }
    }
}
