package com.mccarty.ritmo.model

data class AlbumXX(
    val album_type: String = "",
    val artists: List<Artist> = emptyList(),
    val available_markets: List<String> = emptyList(),
    val copyrights: List<Copyright> = emptyList(),
    val external_ids: ExternalIds = ExternalIds(),
    val external_urls: ExternalUrlsX = ExternalUrlsX(),
    val genres: List<Any> = emptyList(),
    val href: String = "",
    val id: String = "",
    val images: List<Image> = emptyList(),
    val label: String = "",
    val name: String = "",
    val popularity: Int = 0,
    val release_date: String = "",
    val release_date_precision: String = "",
    val total_tracks: Int = 0,
    val tracks: Tracks = Tracks(),
    val type: String = "",
    val uri: String = "",
)

data class Copyright(
    val text: String = "",
    val type: String = "",
)

data class Item(
    val artists: List<Artist>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_urls: ExternalUrlsX,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val name: String,
    val preview_url: String,
    val track_number: Int,
    val type: String,
    val uri: String
)
