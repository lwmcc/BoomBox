package com.mccarty.ritmo.ui

import  androidx.compose.ui.graphics.Color as Color
import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.viewmodel.Playlist
import com.mccarty.ritmo.viewmodel.PlaylistNames

class ItemColor {
    fun textColor(
        playlist: Playlist?,
        mainItem: MainItem,
        trackUri: String?,
        primary: Color,
        onBackground: Color,
    ): Color {
        if (playlist == null || mainItem.track == null || mainItem.track?.uri == null) {
            return onBackground
        }

        if (mainItem.uri == trackUri) {
            return primary
        }

        return when (playlist.name) {
            PlaylistNames.RECOMMENDED_PLAYLIST -> {
                onBackground
            }

            PlaylistNames.RECENTLY_PLAYED, PlaylistNames.USER_PLAYLIST -> {
                if (playlist.uri == mainItem.track?.uri) {
                    primary
                } else {
                    onBackground
                }
            }

            else -> {
                onBackground
            }
        }
    }

    fun textColor(
        isPlaying: Boolean,
        primary: Color,
        onBackground: Color,
        vararg playlistData: String?,
    ): Color {
        if (playlistData.any { it.isNullOrEmpty() }) {
            return onBackground
        }

        return if (isPlaying && playlistData[0] == playlistData[1] && playlistData[2] == playlistData[3]) {
            primary
        } else {
            onBackground
        }
    }

    companion object {
        fun currentItemColor(): ItemColor {
            return ItemColor()
        }
    }
}