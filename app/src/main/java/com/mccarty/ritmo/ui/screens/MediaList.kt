package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.model.payload.PlaylistItem


@OptIn(ExperimentalGlideComposeApi::class)
@androidx.compose.runtime.Composable
fun MediaList(
    tracks: List<Item>,
    onTrackClick: (Int) -> Unit,
) {

    if (tracks.isNotEmpty()) {
            Text(
                text = stringResource(R.string.recently_played),
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .paddingFromBaseline(top = 40.dp)
                    .fillMaxWidth(),
            )
    }

    tracks.forEachIndexed { index, item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    onTrackClick(index)
                })
                .padding(5.dp),
            shape = MaterialTheme.shapes.extraSmall,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row {
                val imageUrl = item.track.album.images.firstOrNull()?.url
                GlideImage(
                    model = imageUrl,
                    contentDescription = "", // TODO: add description
                    modifier = Modifier.size(100.dp)
                )

                Column(modifier = Modifier.padding(start = 20.dp)) {
                    Text(
                        text = item.track.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth(),
                    )
                    Text(
                        text = item.track.album.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth()
                    )
                    if (item.track.artists.isNotEmpty()) {
                        Text(
                            text = item.track.artists[0].name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .paddingFromBaseline(top = 25.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@androidx.compose.runtime.Composable
fun MediaPlayList(
    list: List<PlaylistItem>,
    onTrackClick: (PlaylistItem, Int) -> Unit,
) {
    LazyColumn {
        list.forEachIndexed { index, item  ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            onTrackClick(item, index)
                        })
                        .padding(5.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row {
                        val imageUrl = item.track.album.images.firstOrNull()?.url
                        GlideImage(
                            model = imageUrl,
                            contentDescription = "", // TODO: add description
                            modifier = Modifier.size(100.dp)
                        )

                        Column(modifier = Modifier.padding(start = 20.dp)) {
                            Text(
                                text = item.track.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .paddingFromBaseline(top = 25.dp)
                                    .fillMaxWidth(),
                            )
                            Text(
                                text = item.track.album.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .paddingFromBaseline(top = 25.dp)
                                    .fillMaxWidth()
                            )
                            if (item.track.artists.isNotEmpty()) {
                                Text(
                                    text = item.track.artists[0].name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .paddingFromBaseline(top = 25.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}