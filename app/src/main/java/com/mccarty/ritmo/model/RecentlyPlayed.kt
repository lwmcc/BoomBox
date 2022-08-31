package com.mccarty.ritmo.model

class RecentlyPlayed : ArrayList<RecentlyPlayedItem>()

data class RecentlyPlayedItem(
    val context: Context,
    val played_at: String,
    val track: Track
)

data class Context(
    val external_urls: ExternalUrls,
    val href: String,
    val type: String,
    val uri: String
)

data class Track(
    val album: CurrentAlbum,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrlsXXXX,
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

data class Album(
    val album_type: String = "",
    val artists: List<Artist> = emptyList(),
    val available_markets: List<String> = emptyList(),
    val external_urls: ExternalUrlsXX?,
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

data class ArtistX(
    val external_urls: ExternalUrlsXXX,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class ExternalIds(
    val isrc: String = ""
)

data class ExternalUrlsXXXX(
    val spotify: String
)

data class Artist(
    val external_urls: ExternalUrlsX,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class ExternalUrlsXX(
    val spotify: String = ""
)

data class ExternalUrlsXXX(
    val spotify: String = ""
)