package com.mccarty.ritmo.domain.model.payload

class PlaylistData {
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
        val images: List<Image> = emptyList(),
        val name: String,
        val owner: Owner,
        val public: Boolean,
        val snapshot_id: String,
        val tracks: Tracks,
        val type: String,
        val uri: String
    )

    data class ExternalUrls(
        val spotify: String
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
}
