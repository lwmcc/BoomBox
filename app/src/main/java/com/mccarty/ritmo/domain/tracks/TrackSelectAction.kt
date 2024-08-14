package com.mccarty.ritmo.domain.tracks

import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.viewmodel.PlaylistNames

sealed class TrackSelectAction {
    data class TrackSelect(
        val index: Int,
        val uri: String,
        val duration: Long,
        val tracks: List<MainItem>,
        val playlistName: PlaylistNames,
    ) : TrackSelectAction()
    data class PlayTrackWithUri(val playTrackWithUri: String?): TrackSelectAction()
}