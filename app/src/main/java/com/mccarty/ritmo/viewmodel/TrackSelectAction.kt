package com.mccarty.ritmo.viewmodel

import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.model.payload.MainItem
import com.mccarty.ritmo.model.payload.PlaylistItem
import com.mccarty.ritmo.model.TrackDetails

sealed class TrackSelectAction { // TODO: change name
    data class RecentlyPlayedTrackSelect(val items: List<Item>): TrackSelectAction()
    data class PlaylistTrackSelect(val items: List<PlaylistItem>): TrackSelectAction()
    data class DetailsSelect(val trackId: String): TrackSelectAction()
    data class TrackSelect(val index: Int, val uri: String, val tracks: List<MainItem>): TrackSelectAction()
    data class ViewMoreSelect(val index: Int, val tracks: List<MainItem>): TrackSelectAction()
    data class ViewMoreTrackDetailsSelect(val index: Int, val tracks: List<TrackDetails>): TrackSelectAction()
    data class PlayTrackWithUri(val playTrackWithUri: String): TrackSelectAction()
}