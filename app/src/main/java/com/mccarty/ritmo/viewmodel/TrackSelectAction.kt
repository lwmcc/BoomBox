package com.mccarty.ritmo.viewmodel

import com.mccarty.ritmo.model.payload.MainItem

sealed class TrackSelectAction {
    data class TrackSelect(
        val index: Int,
        val uri: String,
        val duration: Long,
        val tracks: List<MainItem>,
        val playlistName: PlaylistNames,
    ) : TrackSelectAction()
    data class PlayTrackWithUri(val playTrackWithUri: String): TrackSelectAction()
}