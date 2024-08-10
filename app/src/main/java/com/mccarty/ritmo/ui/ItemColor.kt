package com.mccarty.ritmo.ui

import  androidx.compose.ui.graphics.Color as Color
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames

class ItemColor {
    fun textColor(playlist: Playlist?, mainItem: MainItem): Color {
        if (playlist == null || mainItem.track == null || mainItem.track?.uri == null) {
            return  Color.Black
        }

        return  when (playlist.name) {
            PlaylistNames.RECOMMENDED_PLAYLIST -> {
                Color.Black
            }
            PlaylistNames.RECENTLY_PLAYED, PlaylistNames.USER_PLAYLIST -> {
                println("ItemColor ***** ${playlist.uri} ----- ${mainItem.track?.uri}")
                if (playlist.uri == mainItem.track?.uri) {
                    Color.Red
                } else {
                  Color.Black
                }
            }
            else -> {
                Color.Black
            }
        }
    }

    companion object {
        fun currentItemColor(): ItemColor {
            return ItemColor()
        }
    }
}