package com.mccarty.ritmo.domain

import com.mccarty.ritmo.domain.model.payload.AddedBy
import com.mccarty.ritmo.domain.model.payload.Album
import com.mccarty.ritmo.domain.model.payload.Context
import com.mccarty.ritmo.domain.model.payload.ExternalIds
import com.mccarty.ritmo.domain.model.payload.ExternalUrls
import com.mccarty.ritmo.domain.model.payload.Followers
import com.mccarty.ritmo.domain.model.payload.Item
import com.mccarty.ritmo.domain.model.payload.LinkedFrom
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.domain.model.payload.Playlist
import com.mccarty.ritmo.domain.model.payload.PlaylistData
import com.mccarty.ritmo.domain.model.payload.PlaylistItem
import com.mccarty.ritmo.domain.model.payload.Restrictions
import com.mccarty.ritmo.domain.model.payload.Track
import com.mccarty.ritmo.domain.model.payload.TrackItem
import com.mccarty.ritmo.viewmodel.PlaylistNames
import  com.mccarty.ritmo.domain.model.payload.PlaylistData.Item as ItemP
import  com.mccarty.ritmo.domain.model.payload.PlaylistData.ExternalUrls as External
import com.mccarty.ritmo.viewmodel.Playlist as PlaylistVM


val album = Album(
    album_type = "albumType",
    artists = emptyList(),
    available_markets = emptyList(),
    external_urls = ExternalUrls(spotify = "spotify"),
    href = "albumHref",
    id = "1234",
    images = emptyList(),
    name = "albumName",
    release_date = "date",
    release_date_precision = "date",
    restrictions = Restrictions(reason = "some reasons"),
    total_tracks = 77,
    type = "albumType",
    uri = "albumUrl",
)

val track = Track(
    album = album,
    artists = emptyList(), //List<ArtistX>,
    available_markets = emptyList(),
    disc_number = 3,
    duration_ms = 30000,
    explicit = true,
    external_ids = ExternalIds(
        ean = "ean",
        isrc = "isrc",
        upc = "upc"
    ),
    external_urls = ExternalUrls(spotify = "spotify"),
    href = "trackHref",
    id = "trackId",
    is_local = false,
    is_playable = true,
    linked_from = LinkedFrom(),
    name = "trackName",
    popularity = 66,
    preview_url = "trackPreviewUrl",
    restrictions = Restrictions(reason = "some reasons"),
    track_number = 33,
    type = "trackType",
    uri = "trackUri"
)

val followers =Followers(
    href = "",
    total = 50,
)

val externalUrls = ExternalUrls(
    spotify = "spotifyString"
)

val addedBy =  AddedBy(
    external_urls = externalUrls,
    followers = followers,
    href = "",
    id = "",
    type = "",
    uri = "",
)

val playlistItem = PlaylistItem(
    added_at = "2",
    added_by = addedBy,
    is_local = true,
    track = track,
)

val playlistItems = mutableListOf(playlistItem)

val playlist = Playlist(
    href = "www.google.com",
    items = playlistItems,
    limit = 50,
    next = "",
    offset = 1,
    previous = "",
    total = 1,
)
val context = Context(
    external_urls = externalUrls,
    href = "contextHref",
    type = "contextType",
    uri = "contextUri"
)

val item = Item(
    context = context,
    played_at = "playedAt",
    track = track,
)

val items = listOf(item)

val tracks = PlaylistData.Tracks(
    href = "some tracks",
    total = 10,
)

val mainItem = MainItem(
    id = "",
    uri = "",
    type = "",
    track = track,
    name = "",
    trackName = "best song ever",
    description = "a good song",
    tracks = tracks,
    images = emptyList(),
)

val trackItem = TrackItem(
    context = context,
    played_at = "5",
    track = track,
)

val owner = PlaylistData.Owner(
    display_name = "",
    external_urls = PlaylistData.ExternalUrls("spotify-url"),
    followers = PlaylistData.Followers(
        href = "",
        total = 50,
    ),
    href = "",
    id = "",
    type = "",
    uri = "",
)

val playlistItemP = ItemP(
    collaborative = true,
    description = "",
    external_urls = External(
        spotify = "spotify-string"
    ),
    href = "",
    id = "",
    images = emptyList(),
    name = "",
    owner = owner,
    public = true,
    snapshot_id = "",
    tracks = tracks,
    type = "",
    uri = "",

)

val playlistVM = PlaylistVM(
    uri = "some-uri",
    index = 5,
    name = PlaylistNames.RECOMMENDED_PLAYLIST,
    tracks = listOf(mainItem),
)

