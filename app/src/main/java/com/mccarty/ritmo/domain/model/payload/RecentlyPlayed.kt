package com.mccarty.ritmo.domain.model.payload

import com.mccarty.ritmo.domain.model.Artist
import com.mccarty.ritmo.ui.screens.CollectionType

data class RecentlyPlayedItem(
    val cursors: Cursors,
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val total: Int,
)

data class Cursors(
    val after: String,
    val before: String
)

interface MainItem2 {
    val type: String
}
open class MainItem(
    open val id: String? = null,
    open val uri: String? = null,
    open val type: String?,
    open val track: Track? = null,
    open val name: String? = null,
    open val trackName: String? = null,
    open val description: String? = null,
    open val tracks: PlaylistData.Tracks? = null,
    open val images: List<Image> = emptyList(),
)
open class TrackItem(
    val context: Context?,
    val played_at: String,
    override val track: Track?,
) : MainItem(
    type = CollectionType.TRACK.collectionType,
    track = track,
)

open class ListItem(
    val collaborative: Boolean,
    override val description: String,
    val external_urls: PlaylistData.ExternalUrls,
    val href: String,
    override val id: String,
    override val images: List<Image>,
    override val name: String,
    val owner: PlaylistData.Owner,
    val public: Boolean,
    val snapshot_id: String,
    override val tracks: PlaylistData.Tracks,
    override val type: String,
    override val uri: String,
) : MainItem(
    id = id,
    uri = uri,
    type = type,
    name = name,
    description = description,
    images = images,
    tracks = tracks,
)

data class Item(
    val context: Context,
    val played_at: String,
    val track: Track?,
)

data class Context(
    val external_urls: ExternalUrls,
    val href: String,
    val type: String,
    val uri: String
)

data class Track(
    val album: Album,
    val artists: List<ArtistX>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Long = 0L,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String?,
    val is_local: Boolean,
    val is_playable: Boolean,
    val linked_from: LinkedFrom,
    val name: String,
    val popularity: Int,
    val preview_url: String,
    val restrictions: Restrictions,
    val track_number: Int,
    val type: String,
    val uri: String
)

data class ExternalUrls(
    val spotify: String
)

data class Album(
    val album_type: String,
    val artists: List<Artist>,
    val available_markets: List<String>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val restrictions: Restrictions,
    val total_tracks: Int,
    val type: String,
    val uri: String
)

data class ArtistX(
    val external_urls: ExternalUrls,
    val followers: Followers,
    val genres: List<String>,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String?,
    val popularity: Int,
    val type: String,
    val uri: String
)

data class ExternalIds(
    val ean: String,
    val isrc: String,
    val upc: String
)

class LinkedFrom

data class Restrictions(
    val reason: String
)

data class Artist(
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

data class Image(
    val height: Int,
    var url: String,
    val width: Int
)

data class Followers(
    val href: String,
    val total: Int
)



