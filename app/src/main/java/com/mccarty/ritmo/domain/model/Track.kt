package com.mccarty.ritmo.domain.model

class RecentlyPlayedTrack : ArrayList<TrackV2Item>()

data class TrackV2Item(
    val context: Context,
    val played_at: String,
    val track: TrackV2?
)

data class ContextV2(
    val external_urls: ExternalUrls,
    val href: String,
    val type: String,
    val uri: String
)

data class TrackV2(
    val album: Album,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrls,
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

data class ExternalUrlsV2(
    val spotify: String
)

data class AlbumV2(
    val album_type: String,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val total_tracks: Int,
    val type: String,
    val uri: String
)

data class ArtistV2(
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class ExternalIdsV2(
    val isrc: String
)

data class ImageV2(
    val height: Int,
    val url: String,
    val width: Int
)