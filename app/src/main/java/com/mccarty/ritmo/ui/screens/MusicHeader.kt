package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mccarty.ritmo.ui.MainImageHeader

@OptIn(ExperimentalGlideComposeApi::class)
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
        )

        Text(
            text = albumName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
        )
        Text(
            text = songName,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
        )
    }
}


