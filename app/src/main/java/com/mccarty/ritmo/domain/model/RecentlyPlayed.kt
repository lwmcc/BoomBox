package com.mccarty.ritmo.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class RecentlyPlayed : ArrayList<RecentlyPlayedItem>()

@Entity(tableName = "recently_played_item")
data class RecentlyPlayedItem(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "context") val context: Context?,
    @ColumnInfo(name = "played_at") val played_at: String?,
    @ColumnInfo(name = "track") val track: Track?
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

data class Recent(
    val cursors: Cursors,
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val total: Int
)

data class Cursors(
    val after: String,
    val before: String
)

class LinkedFrom

data class RestrictionsX(
    val reason: String
)

data class Followers(
    val href: String,
    val total: Int
)
