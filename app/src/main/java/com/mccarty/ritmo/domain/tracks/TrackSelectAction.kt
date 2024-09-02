package com.mccarty.ritmo.domain.tracks

import com.mccarty.ritmo.domain.model.payload.MainItem
import com.mccarty.ritmo.viewmodel.PlaylistNames

sealed class TrackSelectAction {
    data class TrackSelect(
        val index: Int,
        val uri: String? = null,
        val duration: Long? = 0L,
        val tracks: List<MainItem>,
        val playlistName: PlaylistNames,
    ) : TrackSelectAction()
    data class PlayTrackWithUri(val playTrackWithUri: String?): TrackSelectAction()
    data class PlayTrackScrolledToWithUri(val playTrackWithUri: String?): TrackSelectAction()
}