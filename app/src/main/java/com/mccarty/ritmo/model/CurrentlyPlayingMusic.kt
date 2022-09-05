package com.mccarty.ritmo.model

//data class CurrentlyPlaying(
//    val currently_playing: CurrentlyPlayingX,
//    val queue: List<CurrentQueue>
//)

data class CurrentlyPlayingX(
    val album: CurrentAlbum,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrlsXXX,
    val href: String,
    val id: String,
    val is_local: Boolean,
    val name: String,
    val popularity: Int,
    val preview_url: String,
    val track_number: Int,
    val type: String,
    val uri: String
)

//data class Queue(
//    val album: AlbumX,
//    val artists: List<ArtistXXX>,
//    val available_markets: List<String>,
//    val disc_number: Int,
//    val duration_ms: Int,
//    val explicit: Boolean,
//    val external_ids: ExternalIdsX,
//    val external_urls: ExternalUrlsXXXXXXX,
//    val href: String,
//    val id: String,
//    val is_local: Boolean,
//    val name: String,
//    val popularity: Int,
//    val preview_url: String,
//    val track_number: Int,
//    val type: String,
//    val uri: String
//)

//data class AlbumX(
//    val album_type: String,
//    val artists: List<ArtistXX>,
//    val available_markets: List<String>,
//    val external_urls: ExternalUrlsXXXXX,
//    val href: String,
//    val id: String,
//    val images: List<ImageX>,
//    val name: String,
//    val release_date: String,
//    val release_date_precision: String,
//    val total_tracks: Int,
//    val type: String,
//    val uri: String
//)

data class ArtistXXX(
    val external_urls: ExternalUrlsXXXXXX,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class ExternalIdsX(
    val isrc: String
)

data class ExternalUrlsXXXXXXX(
    val spotify: String
)

data class ArtistXX(
    val external_urls: ExternalUrlsXXXX,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class ExternalUrlsXXXXX(
    val spotify: String
)

data class ImageX(
    val height: Int,
    val url: String,
    val width: Int
)

data class ExternalUrlsXXXXXX(
    val spotify: String
)