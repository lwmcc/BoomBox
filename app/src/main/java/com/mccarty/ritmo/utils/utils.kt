package com.mccarty.ritmo.utils

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

fun processPlaylist(response: Response<JsonObject>): List<PlaylistItem> {
    return if(response.isSuccessful) {
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

fun processRecentlyPlayed(response: Response<JsonObject>): List<RecentlyPlayedItem> {
    return if(response.isSuccessful) {
        try {
            val items = response.body()?.getAsJsonArray(ITEMS)
            val json = items?.asJsonArray
            Gson().fromJson(json, RecentlyPlayed::class.java).toList()
        } catch (je: JSONException) {
            emptyList()
        } catch (npe: NullPointerException) {
            emptyList()
        }
    } else {
        emptyList()
    }
}

fun processQueue(response: Response<JsonObject>): Pair<CurrentlyPlaying, List<CurrentQueueItem>> {
     return if(response.isSuccessful) {
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
         } catch(cce: ClassCastException) {
             Pair(CurrentlyPlaying(), emptyList())
         }
    } else {
         Pair(CurrentlyPlaying(), emptyList())
     }
}

fun List<Image>.getImageUrlFromList(index: Int): String {
    if(index < 0 || index >= this.size) return ""

    var imageUrl = ""
    imageUrl = this.let {
        if(this.isNotEmpty()) {
            this[index].url
        } else {
            ""
        }
    }.toString()
    return imageUrl
}

fun Int.convertBitmapFromDrawable(resources: Resources): Bitmap = BitmapFactory.decodeResource(resources, this)
