package com.mccarty.ritmo.model.payload

import com.google.gson.annotations.SerializedName

class PlaylistData{
    data class PlaylistItem(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String,
    val total: Int
)

data class Item(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val `public`: Boolean,
    val snapshot_id: String,
    val tracks: Tracks,
    val type: String,
    val uri: String
)

data class ExternalUrls(
    val spotify: String
)

data class Image(
    val height: Int,
    val url: String,
    val width: Int
)

data class Owner(
    val display_name: String,
    val external_urls: ExternalUrls,
    val followers: Followers,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class Tracks(
    val href: String,
    val total: Int
)

data class Followers(
    val href: String,
    val total: Int
)


    /*

    data class Playlist(
        val href: String,
        val limit: String,
        val next: String,
        val previous: String,
        val offset: Int,
        val total: Int,


*//*        val collaborative: Boolean,
        val description: String,
        val external_urls: ExternalUrls,
        val followers: Followers,
        val id: String,
        val images: List<Image>,
        val name: String,
        val owner: Owner,
        val public: Boolean,
        val snapshot_id: String,
        val tracks: Tracks,
        val type: String,
        val uri: String*//*
    )

    data class ExternalUrls(
        val spotify: String
    )

    data class Followers(
        val href: String,
        val total: Int
    )

    data class Image(
        val height: Int,
        val url: String,
        val width: Int
    )

    data class Owner(
        val display_name: String,
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
        val followers: Followers,
        val href: String,
        val id: String,
        val type: String,
        val uri: String
    )

    data class Tracks(
        val href: String,
        val items: List<Item>,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: String,
        val total: Int
    )

    data class Item(
        val added_at: String,
        val added_by: AddedBy,
        val is_local: Boolean,
        val track: Track
    )

    data class AddedBy(
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
        val followers: Followers,
        val href: String,
        val id: String,
        val type: String,
        val uri: String
    )

    data class Track(
        val album: Album,
        val artists: List<ArtistX>,
        val available_markets: List<String>,
        val disc_number: Int,
        val duration_ms: Int,
        val explicit: Boolean,
        val external_ids: ExternalIds,
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
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

    data class Album(
        val album_type: String,
        val artists: List<Artist>,
        val available_markets: List<String>,
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
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
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
        val followers: Followers,
        val genres: List<String>,
        val href: String,
        val id: String,
        val images: List<Image>,
        val name: String,
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

    data class RestrictionsX(
        val reason: String
    )

    data class Artist(
        val external_urls: com.mccarty.ritmo.model.payload.ExternalUrls,
        val href: String,
        val id: String,
        val name: String,
        val type: String,
        val uri: String
    )*/
}
