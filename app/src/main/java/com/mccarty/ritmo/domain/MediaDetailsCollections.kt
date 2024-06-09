package com.mccarty.ritmo.domain

import com.mccarty.ritmo.model.TrackDetails
import com.mccarty.ritmo.model.payload.ArtistX
import com.mccarty.ritmo.model.payload.Image
import com.mccarty.ritmo.model.payload.MainItem
import javax.inject.Inject

class MediaDetailsCollections @Inject constructor(): MediaDetails {
    companion object Factory {
        fun create(): MediaDetailsCollections = MediaDetailsCollections()
    }
    override fun mediaDetails(tracks: List<Any>): List<Details> {
        return when (tracks.firstOrNull()) {
            is TrackDetails -> {
                tracks.map {
                    it as TrackDetails
                    Details(
                        albumName = it.albumName,
                        trackName = it.trackName,
                        explicit = it.explicit,
                        artists = it.artists,
                        images = it.images,
                        trackId = it.id,
                        uri = it.uri,
                        type = it.type,
                    )
                }
            }

            is MainItem -> {
                tracks.map {
                    it as MainItem
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

            else -> {
                emptyList()
            }
        }
    }
}

data class Details(
    val albumName: String?,
    val trackName: String?,
    val explicit: Boolean = false,
    val artists: List<ArtistX>? = emptyList(),
    val images: List<Image>? = emptyList(),
    val trackId: String?,
    val uri: String?,
    val type: String?,
)