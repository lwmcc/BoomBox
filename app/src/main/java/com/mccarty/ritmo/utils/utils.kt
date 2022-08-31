package com.mccarty.ritmo.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mccarty.ritmo.model.*
import com.mccarty.ritmo.utils.Constants.CURRENTLY_PLAYING
import com.mccarty.ritmo.utils.Constants.ITEMS
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

fun processQueue(response: Response<JsonObject>): Pair<CurrentAlbum, List<CurrentQueueItem>> {
     return if(response.isSuccessful) {
         try {
             val currentlyPlaying = response.body()?.getAsJsonObject(CURRENTLY_PLAYING)
             val json = currentlyPlaying?.asJsonObject
             val currentAlbum = Gson().fromJson(json, CurrentAlbum::class.java)

             val queue = response.body()?.getAsJsonArray("queue")
             val jsonQueue = queue?.asJsonArray
             val currentQueue = Gson().fromJson(jsonQueue, CurrentQueue::class.java).toList()

             Pair(currentAlbum, currentQueue)
         } catch (je: JSONException) {
             Pair(CurrentAlbum(), emptyList())
         } catch (npe: NullPointerException) {
             Pair(CurrentAlbum(), emptyList())
         } catch(cce: ClassCastException) {
             Pair(CurrentAlbum(), emptyList())
         }
    } else {
         Pair(CurrentAlbum(), emptyList())
     }
}