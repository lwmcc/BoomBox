package com.mccarty.ritmo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainHeader(
    imageUrl: String,
    artistName: String,
    albumName: String,
    songName: String,
    modifier: Modifier = Modifier,
) {
    Column {
        GlideImage(
            model = imageUrl,
            contentDescription = "",
            modifier = Modifier.size(300.dp),
        )

        Text(
            text = artistName,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
        )

        Text(
            text = albumName,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier
                .paddingFromBaseline(top = 25.dp)
                .fillMaxWidth(),
        )
    }
    Text(
        text = songName,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        modifier = Modifier
            .paddingFromBaseline(top = 25.dp)
            .fillMaxWidth(),
    )
}


