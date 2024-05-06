package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mccarty.ritmo.R
import com.mccarty.ritmo.model.payload.Item


@androidx.compose.runtime.Composable
fun MediaList(
    list: List<Item>,
    onTrackClick: (Item) -> Unit,
) {

    if (list.isNotEmpty()) {
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

    list.forEach {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    onTrackClick(it)
                })
                .padding(5.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            Column() {
                Text(
                    text = "${it.track?.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth(),
                )
                Text(
                    text = "${it.track?.album?.name}",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .fillMaxWidth()
                )
                if (it.track?.artists?.isNotEmpty() == true) {
                    Text(
                        text = "${it.track.artists.get(0).name}",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .paddingFromBaseline(top = 25.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}