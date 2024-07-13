package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mccarty.ritmo.ui.MainImageHeader

@Composable
fun MainHeader(
    imageUrl: String,
    artistName: String,
    albumName: String,
    songName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(
                start = 16.dp,
                bottom = 16.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        MainImageHeader(
            imageUrl,
            400.dp,
            50.dp,
            50.dp,
            modifier,
            )
        Text(
            text = artistName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = albumName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = songName,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


