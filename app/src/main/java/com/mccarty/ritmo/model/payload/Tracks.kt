package com.mccarty.ritmo.model.payload
data class Playlist(
    val href: String,
    val items: List<PlaylistItem>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
)

data class PlaylistItem(
    val added_at: String,
    val added_by: AddedBy,
    val is_local: Boolean,
    val track: Track
)

data class AddedBy(
    val external_urls: ExternalUrls,
    val followers: Followers,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class PlaylistTrack(
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
    val is_playable: Boolean,
    val linked_from: LinkedFrom,
    val name: String,
    val popularity: Int,
    val preview_url: String,
    val restrictions: RestrictionsX,
    val track_number: Int,
    val type: String,
    val uri: String
)

data class RestrictionsX(
    val reason: String
)