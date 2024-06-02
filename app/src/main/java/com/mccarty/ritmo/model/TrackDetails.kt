package com.mccarty.ritmo.model

import com.mccarty.ritmo.model.payload.ArtistX
import com.mccarty.ritmo.model.payload.Image

data class TrackDetails(
    val id: String,
    val uri: String,
    val images: List<Image> = emptyList(),
    val artists: List<ArtistX> = emptyList(),
    val trackName: String,
    val albumName: String,
    val explicit: Boolean = true,
)
