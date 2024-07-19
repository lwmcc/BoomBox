package com.mccarty.ritmo.domain.model

data class CurrentlyPlayingTrack(
    val actions: Actions,
    val context: Context,
    val currently_playing_type: String,
    val is_playing: Boolean,
    val item: ItemV2,
    val progress_ms: Int,
    val timestamp: Long
)

data class Actions(
    val disallows: Disallows
)

data class ItemV2(
    val album: Album = Album(),
    val artists: List<ArtistX> = emptyList(),
    val available_markets: List<String> = emptyList(),
    val disc_number: Int = 0,
    val duration_ms: Int = 0,
    val explicit: Boolean = true,
    val external_ids: ExternalIds = ExternalIds(),
    val external_urls: ExternalUrls = ExternalUrls(),
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

data class Disallows(
    val resuming: Boolean
)

//data class ExternalUrls(
//    val spotify: String
//)

//data class Album(
//    val album_type: String,
//    val artists: List<ArtistX>,
//    val available_markets: List<String>,
//    val external_urls: ExternalUrls,
//    val href: String,
//    val id: String,
//    val images: List<Image>,
//    val name: String,
//    val release_date: String,
//    val release_date_precision: String,
//    val total_tracks: Int,
//    val type: String,
//    val uri: String
//)

//data class ArtistX(
//    val external_urls: ExternalUrls,
//    val href: String,
//    val id: String,
//    val name: String,
//    val type: String,
//    val uri: String
//)

//data class ExternalIds(
//    val isrc: String
//)
//
//data class Image(
//    val height: Int,
//    val url: String,
//    val width: Int
//)