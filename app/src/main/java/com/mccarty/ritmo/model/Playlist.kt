package com.mccarty.ritmo.model

class Playlist : ArrayList<PlaylistItem>()

data class PlaylistItem(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val primary_color: Any,
    val `public`: Boolean,
    val snapshot_id: String,
    val tracks: Tracks,
    val type: String,
    val uri: String
)

data class ExternalUrls(
    val spotify: String = ""
)

data class Owner(
    val display_name: String,
    val external_urls: ExternalUrlsX,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class Tracks(
    val href: String = "",
    val total: Int = 0,
)

data class ExternalUrlsX(
    val spotify: String = ""
)