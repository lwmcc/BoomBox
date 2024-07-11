package com.mccarty.ritmo.ui

import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames

class ItemColor {

    fun textColor(playlist: Playlist?, mainItem: MainItem): androidx.compose.ui.graphics.Color {
        return  when (playlist?.name) {
            PlaylistNames.RECOMMENDED_PLAYLIST -> {
                androidx.compose.ui.graphics.Color.Black
            }
            PlaylistNames.RECENTLY_PLAYED -> {
                if (playlist.uri == mainItem.track?.uri) {
                    androidx.compose.ui.graphics.Color.Red
                } else {
                    androidx.compose.ui.graphics.Color.Black
                }
            }
            PlaylistNames.USER_PLAYLIST -> {
                androidx.compose.ui.graphics.Color.Black
            }
            null -> {
                androidx.compose.ui.graphics.Color.Black
            }
        }
    }

    companion object {
        fun currentItemColor(): ItemColor {
            return ItemColor()
        }

    }
}