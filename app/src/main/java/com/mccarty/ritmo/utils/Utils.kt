package com.mccarty.ritmo.utils

import com.mccarty.ritmo.domain.Details
import com.mccarty.ritmo.domain.model.TrackDetails
import com.mccarty.ritmo.domain.model.payload.Item
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.domain.model.payload.PlaylistItem as PItem

fun List<Item>.createTrackDetailsFromItems(): List<MainItem> {
    if (this.isEmpty()) {
        return emptyList()
    }
    return this.filter { it.track != null }.map {
        TrackDetails(
            id = it.track?.id ?: "",
            uri = it.track?.uri ?: "",
            images = it.track?.album?.images ?: emptyList(),
            trackName = it.track?.name ?: "",
            albumName = it.track?.album?.name ?: "",
            artists = it.track?.artists ?: emptyList(),
            explicit = it.track?.explicit ?: true,
            track = it.track,
        )
    }.distinctBy { trackId -> trackId.track?.id }
}

fun List<PItem>.createTrackDetailsFromPlayListItems(): List<TrackDetails> {
    if (this.isEmpty()) {
        return emptyList()
    }
    return this.filter { it.track != null }.map {
        TrackDetails(
            id = it.track?.id ?: "",
            uri = it.track?.uri ?: "",
            images = it.track?.album?.images ?: emptyList(),
            trackName = it.track?.name ?: "",
            albumName = it.track?.album?.name ?: "",
            artists = it.track?.artists ?: emptyList(),
            explicit = it.track?.explicit ?: true,
            track = it.track,
        )
    }.distinctBy { it.id }
}

fun Float.positionProduct(factor: Long): Long {
    return this.toLong() * factor
}

inline fun<reified P: Number, reified D: Number> trackHasEnded(position: P, duration: D): Boolean {
    return position.toLong().compareTo(duration.toLong()) == 0
}

fun List<Details>.createListFromDetails(recent: List<MainItem>): List<Details> {
    return this.ifEmpty {
        recent.map {
            Details(
                albumName = it.track?.album?.name,
                trackName = it.track?.name,
                explicit = it.track?.explicit ?: false,
                artists = it.track?.artists,
                images = it.track?.album?.images,
                trackId = it.track?.id,
                uri = it.track?.uri,
                type = it.track?.type,
            )
        }
    }
}