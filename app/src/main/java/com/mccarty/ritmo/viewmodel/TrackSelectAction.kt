package com.mccarty.ritmo.viewmodel

import com.mccarty.ritmo.model.PlaylistItem
import com.mccarty.ritmo.model.payload.Item
sealed class TrackSelectAction {
    data class RecentlyPlayedTrackSelect(val items: List<Item>): TrackSelectAction()
    data class PlaylistTrackSelect(val items: List<PlaylistItem>): TrackSelectAction()
}