package com.mccarty.ritmo.model

data class CurrentAlbum(
    val album: Album? = Album(external_urls = ExternalUrlsXX()),
    val artists: List<ArtistX> = emptyList(),
    val available_markets: List<String> = emptyList(),
    val disc_number: Int = 0,
    val duration_ms: Int = 0,
    val explicit: Boolean = true,
    val external_ids: ExternalIds = ExternalIds(),
    val external_urls: ExternalUrlsXXX = ExternalUrlsXXX(),
    val href: String = "",
    val id: String = "",
    val is_local: Boolean = true,
    val name: String = "",
    val popularity: Int = 0,
    val preview_url: String = "",
    val track_number: Int = 0,
    val type: String = "",
    val uri: String = "",
)