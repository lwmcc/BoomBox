package com.mccarty.ritmo.model

import com.mccarty.ritmo.model.payload.Image

data class Album(
    val album_type: String ="",
    val artists: List<ArtistX> = emptyList(),
    val available_markets: List<String> = emptyList(),
    val external_urls: ExternalUrlsXXX = ExternalUrlsXXX(),
    val href: String = "",
    val id: String = "",
    val images: List<Image> = emptyList(),
    val name: String = "",
    val release_date: String = "",
    val release_date_precision: String = "",
    val total_tracks: Int = 0,
    val type: String = "",
    val uri: String = "",
)