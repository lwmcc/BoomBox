package com.mccarty.ritmo.model

import com.mccarty.ritmo.model.payload.ArtistX
import com.mccarty.ritmo.model.payload.Image
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.model.payload.Track

data class TrackDetails(
    override val id: String,
    override val uri: String,
    override val images: List<Image> = emptyList(),
    val artists: List<ArtistX> = emptyList(),
    override val trackName: String,
    override val track: Track?,
    val albumName: String,
    val explicit: Boolean = true,
) : MainItem(
    type = "track",
    name = albumName,
    trackName = trackName,
    track = track,
)
