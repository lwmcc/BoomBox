package com.mccarty.ritmo.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.utils.Constants.CURRENTLY_PLAYING
import com.mccarty.ritmo.utils.Constants.ITEMS
import com.mccarty.ritmo.utils.Constants.QUEUE
import org.json.JSONException
import retrofit2.Response
import java.lang.NullPointerException
import java.lang.NumberFormatException
import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.model.payload.PlaylistItem as PItem

fun processPlaylist(response: Response<JsonObject>): List<PlaylistItem> {
    return if (response.isSuccessful) {
        println("MainViewModel ***** Array ${response.body()}")
        try {
            val items = response.body()?.getAsJsonArray(ITEMS)
            val jsonArray = items?.asJsonArray
            Gson().fromJson(jsonArray, Playlist::class.java).toList()
        } catch (je: JSONException) {
            emptyList()
        } catch (npe: NullPointerException) {
            emptyList()
        }
    } else {
        emptyList()
    }
}

fun processRecentlyPlayed(response: Response<JsonObject>): List<TrackV2Item> {
    return if (response.isSuccessful) {
        try {
            val items = response.body()?.getAsJsonArray(ITEMS)
            val json = items?.asJsonArray
            val list = Gson().fromJson(json, RecentlyPlayedTrack::class.java).toList()
            list
        } catch (je: JSONException) {
            emptyList()
        } catch (npe: NullPointerException) {
            emptyList()
        }
    } else if (response.code() == 429) {
        try {
            val header = response.headers().get("Retry-After")?.toInt()
            emptyList()
        } catch (nfe: NumberFormatException) {
            emptyList()
        }
    } else {
        emptyList()
    }
}

fun processQueue(response: Response<JsonObject>): Pair<CurrentlyPlaying, List<CurrentQueueItem>> {
    return if (response.isSuccessful) {
        try {
            val currentlyPlaying = response.body()?.getAsJsonObject(CURRENTLY_PLAYING)
            val json = currentlyPlaying?.asJsonObject
            var playing = Gson().fromJson(json, CurrentlyPlaying::class.java)

            val queue = response.body()?.getAsJsonArray(QUEUE)
            val jsonQueue = queue?.asJsonArray
            val currentQueue = Gson().fromJson(jsonQueue, CurrentQueue::class.java).toList()

            Pair(playing, currentQueue)
        } catch (je: JSONException) {
            Pair(CurrentlyPlaying(), emptyList())
        } catch (npe: NullPointerException) {
            Pair(CurrentlyPlaying(), emptyList())
        } catch (cce: ClassCastException) {
            Pair(CurrentlyPlaying(), emptyList())
        }
    } else {
        Pair(CurrentlyPlaying(), emptyList())
    }
}

fun processAlbumData(response: Response<JsonObject>): AlbumXX {
    return if (response.isSuccessful) {
        try {
            val json = response.body()?.asJsonObject
            val g = Gson().fromJson(json, AlbumXX::class.java)
            return g
        } catch (je: JSONException) {
            AlbumXX()
        } catch (npe: NullPointerException) {
            AlbumXX()
        } catch (cce: ClassCastException) {
            AlbumXX()
        }
    } else {
        AlbumXX()
    }
}

fun processCurrentlyPlaying(response: Response<JsonObject>): Pair<Boolean, ItemV2> {
    return if (response.isSuccessful) {
        when (response.code()) {
            200 -> {
                try {
                    val json = response.body()?.asJsonObject
                    val g = Gson().fromJson(json, CurrentlyPlayingTrack::class.java)
                    Pair(g.is_playing, g.item)
                } catch (je: JSONException) {
                    Pair(false, ItemV2())
                } catch (npe: NullPointerException) {
                    Pair(false, ItemV2())
                } catch (cce: ClassCastException) {
                    Pair(false, ItemV2())
                }
            }

            204 -> { // Code 204 means it is not currently playing anything
                Pair(false, ItemV2())
            }

            else -> {
                Pair(false, ItemV2())
            }
        }
    } else {
        Pair(false, ItemV2())
    }
}

fun List<Image>.getImageUrlFromList(index: Int): String {
    if (index < 0 || index >= this.size) return ""

    var imageUrl = ""
    imageUrl = this.let {
        if (this.isNotEmpty()) {
            this[index].url
        } else {
            ""
        }
    }.toString()
    return imageUrl
}

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
        )
    }.distinctBy { it.id }
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
        )
    }.distinctBy { it.id }
}

fun Float.positionProduct(factor: Long): Long {
    return this.toLong() * factor
}

fun Long.quotientOf(divisor: Long): Long {
    if (this == null) return 0L
    return if (this > 0L) {
        this / divisor
    } else {
        0L
    }
}
