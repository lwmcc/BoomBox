package com.mccarty.ritmo.utils

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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

fun processPlaylist(response: Response<JsonObject>): List<PlaylistItem> {
    return if (response.isSuccessful) {
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

fun processRecentlyPlayed(response: Response<JsonObject>): Pair<Int, List<RecentlyPlayedItem>> {
    val retryInterval = 0
    val oneHour = 3_600
    return if (response.isSuccessful) {
        try {
            val items = response.body()?.getAsJsonArray(ITEMS)
            val json = items?.asJsonArray
            val list = Gson().fromJson(json, RecentlyPlayed::class.java).toList()
            Pair(retryInterval, list)
        } catch (je: JSONException) {
            Pair(retryInterval, emptyList())
        } catch (npe: NullPointerException) {
            Pair(retryInterval, emptyList())
        }
    }
    else if(response.code() == 429) {
        try {
            val header = response.headers().get("Retry-After")?.toInt()
            Pair(header ?: oneHour, emptyList())
        } catch (nfe: NumberFormatException) {
            Pair(0, emptyList())
        }
    } else {
        Pair(retryInterval, emptyList())
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

fun processCurrentlyPlaying(response: Response<JsonObject>): Boolean? {
    return if (response.isSuccessful) {
        when (response.code()) {
            200 -> {
                try {
                    val isPlaying = response.body()?.getAsJsonPrimitive("is_playing")
                    val json = isPlaying?.asJsonPrimitive
                    json?.asBoolean
                } catch (je: JSONException) {
                    false
                } catch (npe: NullPointerException) {
                    false
                } catch (cce: ClassCastException) { // TODO: add these where needed
                    false
                }
            }
            204 -> { // Is not currently playing anything
                println("MainViewModel 204 ")
                false
            }
            else -> {
                false
            }
        }
    } else {
        false
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

// TODO: generics??
fun currentTimeSecondsd(time: Long): Long {
    return time / 60
}