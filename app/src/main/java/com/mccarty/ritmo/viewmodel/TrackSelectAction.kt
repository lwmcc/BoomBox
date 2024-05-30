package com.mccarty.ritmo.viewmodel

import com.mccarty.ritmo.model.payload.Item
import com.mccarty.ritmo.model.payload.PlaylistItem

sealed class TrackSelectAction {
    data class RecentlyPlayedTrackSelect(val items: List<Item>): TrackSelectAction()
    data class PlaylistTrackSelect(val items: List<PlaylistItem>): TrackSelectAction()
    data class DetailsSelect(val trackId: String): TrackSelectAction()
}